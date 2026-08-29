package com.buzzingjava.waitlist;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitlistApiController.class);

    private final WaitlistService waitlistService;

    public WaitlistApiController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping("/count")
    public WaitlistService.CountResponse count() {
        return waitlistService.currentCount();
    }

    @PostMapping
    public ResponseEntity<WaitlistService.MessageResponse> join(
            @RequestBody WaitlistService.WaitlistRequest request,
            HttpServletRequest httpRequest) {
        String ip = extractClientIp(httpRequest);
        WaitlistService.WaitlistRequest requestWithIp = new WaitlistService.WaitlistRequest(
                request.name(),
                request.email(),
                request.party(),
                request.expectations(),
                request.otherExpectation(),
                request.timestamp(),
                request.utmSource(),
                request.utmMedium(),
                request.utmCampaign(),
                ip);
        return ResponseEntity.ok(waitlistService.join(requestWithIp));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedForHeader = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();

        LOGGER.info("[IP-DEBUG] raw X-Forwarded-For={}", forwardedForHeader);
        LOGGER.info("[IP-DEBUG] raw remoteAddr={}", remoteAddr);

        String finalIp = remoteAddr;
        if (forwardedForHeader != null && !forwardedForHeader.isBlank()) {
            String firstForwardedIp = Arrays.stream(forwardedForHeader.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .findFirst()
                    .orElse("");
            if (!firstForwardedIp.isEmpty()) {
                finalIp = firstForwardedIp;
            }
        }

        LOGGER.info("[IP-DEBUG] final client IP={}", finalIp);
        return finalIp;
    }
}