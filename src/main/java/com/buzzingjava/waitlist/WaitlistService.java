package com.buzzingjava.waitlist;

import com.buzzingjava.config.SiteProperties;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WaitlistService {
    private static final long COUNT_CACHE_TTL_MILLIS = 60_000L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitlistService.class);

    private final Optional<GoogleSheetsService> googleSheetsService;
    private volatile CountCache countCache = new CountCache(0, 0L);

    public WaitlistService(
            SiteProperties site,
            Optional<GoogleSheetsService> googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    public CountResponse currentCount() {
        CountCache cached = countCache;
        long now = System.currentTimeMillis();
        if (now - cached.cachedAt() < COUNT_CACHE_TTL_MILLIS) {
            return new CountResponse(cached.count());
        }
        synchronized (this) {
            cached = countCache;
            now = System.currentTimeMillis();
            if (now - cached.cachedAt() >= COUNT_CACHE_TTL_MILLIS) {
                int count = readSignupCount();
                countCache = new CountCache(count, now);
                cached = countCache;
            }
        }
        return new CountResponse(cached.count());
    }

    public MessageResponse join(WaitlistRequest request) {
        validate(request);
        String email = request.email().trim();
        if (googleSheetsService.isPresent() && googleSheetsService.get().getEmails().stream()
                .skip(1)
                .map(String::trim)
                .anyMatch(existing -> existing.equalsIgnoreCase(email))) {
            return new MessageResponse("You are on the list!");
        }
        WaitlistSheetRow sheetRow = new WaitlistSheetRow(
            request.name().trim(),
            email,
            request.party().trim(),
            String.join(", ", request.expectations()),
            request.otherExpectation() == null ? "" : request.otherExpectation().trim());
        googleSheetsService.ifPresent(service -> service.append(sheetRow));
        countCache = new CountCache(0, 0L);
        return new MessageResponse("You are on the list!");
    }

    private int readSignupCount() {
        try {
            return (int) googleSheetsService.map(GoogleSheetsService::getEmails)
                    .orElse(List.of()).stream()
                    .skip(1)
                    .filter(email -> !email.trim().isEmpty())
                    .count();
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to read the waitlist count from Google Sheets; using 0.", exception);
            return 0;
        }
    }

    private void validate(WaitlistRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Waitlist request is required.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (request.email() == null || !EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (request.party() == null || request.party().isBlank()) {
            throw new IllegalArgumentException("Party preference is required.");
        }
        if (request.expectations() == null || request.expectations().isEmpty()) {
            throw new IllegalArgumentException("At least one expectation is required.");
        }
    }

    private record CountCache(int count, long cachedAt) {}

    public record CountResponse(int count) {}

    public record MessageResponse(String message) {}

    public record WaitlistRequest(String name, String email, String party,
                                  java.util.List<String> expectations, String otherExpectation) {}
}