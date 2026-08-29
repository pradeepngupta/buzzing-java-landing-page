package com.buzzingjava.web;

import com.buzzingjava.config.SiteProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.ui.Model;

public final class PageModel {
    private PageModel() {}

    public static void addTo(Model model, SiteProperties site) {
        model.addAttribute("site", site);
        model.addAttribute("bookJson", bookJson(site));
        model.addAttribute("eventJson", eventJson(site));
        model.addAttribute("faqJson", faqJson(site));
    }

    private static String bookJson(SiteProperties site) {
        return writeJson(Map.of("@context", "https://schema.org", "@type", "Book",
                "name", site.book().title(), "alternateName", site.book().subtitle(),
                "description", site.book().description(), "datePublished", site.book().launchDateIso(),
                "author", Map.of("@type", "Person", "name", site.book().author()),
                "inLanguage", site.book().language(), "genre", site.book().genre()));
    }

    private static String eventJson(SiteProperties site) {
        if (!site.launchEvent().enabled()) return "";
        var event = new java.util.HashMap<String, Object>();
        event.put("@context", "https://schema.org");
        event.put("@type", "Event");
        event.put("name", site.launchEvent().name());
        event.put("startDate", site.launchEvent().startDate());
        event.put("eventStatus", "https://schema.org/" + site.launchEvent().eventStatus());
        event.put("eventAttendanceMode", "https://schema.org/" + site.launchEvent().eventAttendanceMode());
        event.put("description", site.book().description());
        if (!site.launchEvent().endDate().isBlank()) event.put("endDate", site.launchEvent().endDate());
        if (!site.launchEvent().location().isBlank()) {
            event.put("location", Map.of("@type", "Place", "name", site.launchEvent().location()));
        }
        return writeJson(event);
    }

    private static String faqJson(SiteProperties site) {
        return writeJson(Map.of("@context", "https://schema.org", "@type", "FAQPage", "mainEntity", site.faq().stream().map(item -> Map.of(
                "@type", "Question", "name", item.question(),
                "acceptedAnswer", Map.of("@type", "Answer", "text", item.answer()))).toList()));
    }

    private static String writeJson(Object value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to create structured data", exception);
        }
    }
}
