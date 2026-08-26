package com.buzzingjava.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "buzzing-java")
public record SiteProperties(
        Book book,
        Cta cta,
        Waitlist waitlist,
        Freebie freebie,
        List<Credibility> credibility,
        List<Teaser> teasers,
        List<Perk> perks,
        Seo seo,
        List<Faq> faq,
        LaunchEvent launchEvent) {

    public record Book(String title, String subtitle, String author, String launchDate,
                       String launchDateIso, String description, String genre, String language) {}

    public record Cta(String label, String mode, String link) {}

    public record Waitlist(Counter counter, List<String> partyOptions, List<String> expectations) {
        public record Counter(boolean enabled, int threshold, int currentCount) {}
    }

    public record Freebie(boolean enabled, int quantity, String title, String description) {}

    public record Credibility(String value, String label) {}

    public record Teaser(String number, String title, String description) {}

    public record Perk(String title, String description) {}

    public record Seo(String title, String description, String canonicalUrl, String ogImage) {}

    public record Faq(String question, String answer) {}

    public record LaunchEvent(boolean enabled, String name, String startDate, String endDate,
                              String eventStatus, String eventAttendanceMode, String location) {}
}
