package com.buzzingjava.web;

import com.buzzingjava.config.SiteProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.ui.Model;

public final class PageModel {
    private PageModel() {}

    public static void addTo(Model model, SiteProperties site) {
        boolean showCounter = site.waitlist().counter().enabled()
                && site.waitlist().counter().currentCount() > site.waitlist().counter().threshold();
        model.addAttribute("site", site);
        model.addAttribute("showCounter", showCounter);
        model.addAttribute("waitlistCount", site.waitlist().counter().currentCount());
        model.addAttribute("faqJson", jsonLd(site));
    }

    private static String jsonLd(SiteProperties site) {
        var graph = new java.util.ArrayList<Map<String, Object>>();
        graph.add(Map.of("@type", "Book", "name", site.book().title(),
                "author", Map.of("@type", "Person", "name", site.book().author()),
                "description", site.book().description(), "datePublished", "2026-12-07",
                "genre", site.book().genre(), "inLanguage", site.book().language()));
        if (site.launchEvent().enabled()) {
            var event = new java.util.HashMap<String, Object>();
            event.put("@type", "Event");
            event.put("name", site.launchEvent().name());
            event.put("startDate", site.launchEvent().startDate());
            event.put("eventStatus", "https://schema.org/" + site.launchEvent().eventStatus());
            event.put("eventAttendanceMode", "https://schema.org/" + site.launchEvent().eventAttendanceMode());
            if (!site.launchEvent().endDate().isBlank()) event.put("endDate", site.launchEvent().endDate());
            graph.add(event);
        }
        graph.add(Map.of("@type", "FAQPage", "mainEntity", site.faq().stream().map(item -> Map.of(
                "@type", "Question", "name", item.question(),
                "acceptedAnswer", Map.of("@type", "Answer", "text", item.answer()))).toList()));
        try {
            return new ObjectMapper().writeValueAsString(Map.of("@context", "https://schema.org", "@graph", graph));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to create structured data", exception);
        }
    }
}
