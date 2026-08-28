package com.buzzingjava.waitlist;

import com.buzzingjava.config.SiteProperties;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WaitlistService {
    private static final int[] INCREMENTS = {1, 3, 5, 10};
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitlistService.class);

    private final SiteProperties.Waitlist.Counter configuration;
    private final AtomicInteger count;
    private final Optional<GoogleSheetsService> googleSheetsService;

    public WaitlistService(
            SiteProperties site,
            Optional<GoogleSheetsService> googleSheetsService,
            @Value("${google.sheets.minimum-display-count:50}") int minimumDisplayCount) {
        configuration = site.waitlist().counter();
        count = new AtomicInteger(minimumDisplayCount);
        this.googleSheetsService = googleSheetsService;
    }

    public CountResponse currentCount() {
        int actualCount = 0;
        try {
            actualCount = googleSheetsService.map(GoogleSheetsService::getRowCount).orElse(0);
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to read the waitlist count from Google Sheets; using 0.", exception);
        }
        int current = Math.max(actualCount, count.get());
        return new CountResponse(current, configuration.enabled() && current > configuration.threshold());
    }

    public CountResponse join(WaitlistRequest request) {
        validate(request);
        WaitlistSheetRow sheetRow = new WaitlistSheetRow(
            request.name().trim(),
            request.email().trim(),
            request.party().trim(),
            String.join(", ", request.expectations()),
            request.otherExpectation() == null ? "" : request.otherExpectation().trim());
        googleSheetsService.ifPresent(service -> service.append(sheetRow));
        int increment = INCREMENTS[ThreadLocalRandom.current().nextInt(INCREMENTS.length)];
        count.addAndGet(increment);
        try {
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000); // Simulate processing delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return currentCount();
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

    public record CountResponse(int count, boolean visible) {}

    public record WaitlistRequest(String name, String email, String party,
                                  java.util.List<String> expectations, String otherExpectation) {}
}