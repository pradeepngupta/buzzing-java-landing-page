package com.buzzingjava.waitlist;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistApiController {
    private final WaitlistService waitlistService;

    public WaitlistApiController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping("/count")
    public WaitlistService.CountResponse count() {
        return waitlistService.currentCount();
    }

    @PostMapping
    public ResponseEntity<WaitlistService.CountResponse> join(
            @RequestBody WaitlistService.WaitlistRequest request) {
        return ResponseEntity.ok(waitlistService.join(request));
    }
}