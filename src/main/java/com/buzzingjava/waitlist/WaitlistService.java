package com.buzzingjava.waitlist;

import com.buzzingjava.config.SiteProperties;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WaitlistService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitlistService.class);

    private final Optional<GoogleSheetsService> googleSheetsService;
    private final Set<String> accepted = ConcurrentHashMap.newKeySet();

    public WaitlistService(
            SiteProperties site,
            Optional<GoogleSheetsService> googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @PostConstruct
    void loadAcceptedEmails() {
        if (googleSheetsService.isEmpty()) {
            return;
        }

        GoogleSheetsService sheetsService = googleSheetsService.get();
        if (!sheetsService.isConfigured()) {
            LOGGER.warn("Google Sheets is not configured; starting with an empty waitlist email set.");
            return;
        }

        List<String> emails = sheetsService.getEmails();
        emails.stream()
                .skip(1)
                .map(WaitlistService::emailKey)
                .filter(email -> !email.isEmpty())
                .forEach(accepted::add);
        LOGGER.info("Loaded {} existing waitlist emails from Google Sheets.", accepted.size());
    }

    public CountResponse currentCount() {
        return new CountResponse(googleSheetsService.isPresent() ? accepted.size() : 0);
    }

    public MessageResponse join(WaitlistRequest request) {
        String requestId = UUID.randomUUID().toString();
        validate(request);
        String email = request.email().trim();
        String key = emailKey(email);
        String ip = request.ip() == null ? "" : request.ip().trim();
        LOGGER.info("Handling waitlist join request id={} email={} ip={} submittedAt={}",
                requestId, email, ip, request.timestamp());
        if (!accepted.add(key)) {
            LOGGER.info("Waitlist join request id={} email={} is already on the list",
                    requestId, email);
            return new MessageResponse("You are on the list!");
        }

        List<String> expectationValues = request.expectations() == null ? List.of() : request.expectations();
        String otherExpectation = request.otherExpectation() == null ? "" : request.otherExpectation().trim();
        List<String> combinedExpectations = new java.util.ArrayList<>(expectationValues.size() + (otherExpectation.isEmpty() ? 0 : 1));
        for (String expectation : expectationValues) {
            if (expectation != null && !expectation.isBlank()) {
                combinedExpectations.add(expectation.trim());
            }
        }
        if (!otherExpectation.isEmpty()) {
            combinedExpectations.add(otherExpectation);
        }

        String timestamp = request.timestamp() == null || request.timestamp().isBlank()
                ? Instant.now().toString()
                : request.timestamp().trim();
        String utmSource = request.utmSource() == null ? "" : request.utmSource().trim();
        String utmMedium = request.utmMedium() == null ? "" : request.utmMedium().trim();
        String utmCampaign = request.utmCampaign() == null ? "" : request.utmCampaign().trim();
        String country = "";
        String countryCode = "";

        WaitlistSheetRow sheetRow = new WaitlistSheetRow(
                timestamp,
                request.name().trim(),
                email,
                request.party().trim(),
                utmSource,
                utmMedium,
                utmCampaign,
                ip,
                country,
                countryCode,
                String.join(", ", combinedExpectations),
                Boolean.toString(request.privacyConsent()));
        try {
            googleSheetsService.ifPresentOrElse(
                    service -> {
                        LOGGER.info("Appending waitlist join request id={} email={} to Google Sheets",
                                requestId, email);
                        service.append(sheetRow);
                        LOGGER.info("Appended waitlist join request id={} email={} to Google Sheets",
                                requestId, email);
                    },
                    () -> LOGGER.warn("Waitlist join request id={} email={} was not saved; "
                            + "Google Sheets service is unavailable", requestId, email));
        } catch (RuntimeException exception) {
            accepted.remove(key);
            throw exception;
        }
        return new MessageResponse("You are on the list!");
    }

    private static String emailKey(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
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
        if (!request.privacyConsent()) {
            throw new IllegalArgumentException("Consent is required.");
        }
    }

    public record CountResponse(int count) {}

    public record MessageResponse(String message) {}

    public record WaitlistRequest(String name, String email, String party,
                                  java.util.List<String> expectations, String otherExpectation,
                                  String timestamp, String utmSource, String utmMedium,
                                  String utmCampaign, String ip, boolean privacyConsent) {}
}