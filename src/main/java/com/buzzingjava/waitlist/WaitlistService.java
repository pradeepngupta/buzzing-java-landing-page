package com.buzzingjava.waitlist;

import com.buzzingjava.config.SiteProperties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class WaitlistService {
    private static final int[] INCREMENTS = {1, 3, 5, 10};

    private final SiteProperties.Waitlist.Counter configuration;
    private final AtomicInteger count;

    public WaitlistService(SiteProperties site) {
        configuration = site.waitlist().counter();
        count = new AtomicInteger(configuration.currentCount());
    }

    public CountResponse currentCount() {
        int current = count.get();
        return new CountResponse(current, configuration.enabled() && current > configuration.threshold());
    }

    public CountResponse join(WaitlistRequest request) {
        int increment = INCREMENTS[ThreadLocalRandom.current().nextInt(INCREMENTS.length)];
        count.addAndGet(increment);
        try {
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1000); // Simulate processing delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return currentCount();
    }

    public record CountResponse(int count, boolean visible) {}

    public record WaitlistRequest(String name, String email, String party,
                                  java.util.List<String> expectations, String otherExpectation) {}
}