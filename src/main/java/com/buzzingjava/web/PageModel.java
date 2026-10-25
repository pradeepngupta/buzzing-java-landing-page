package com.buzzingjava.web;

import com.buzzingjava.config.SiteProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ui.Model;

public final class PageModel {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ORGANIZATION_ID = "https://buzzingjava.com/#organization";
    private static final String ORGANIZATION_LOGO = "https://buzzingjava.com/assets/web-app-manifest-512x512.png";

    private PageModel() {}

    public static void addTo(Model model, SiteProperties site) {
        model.addAttribute("site", site);
        model.addAttribute("assetBasePath", site.seo().basePath());
        model.addAttribute("organizationJson", organizationJson(site));
        model.addAttribute("bookJson", bookJson(site));
        model.addAttribute("eventJson", eventJson(site));
        model.addAttribute("faqJson", faqJson(site));
    }

    private static String organizationJson(SiteProperties site) {
        var logo = Map.of("@type", "ImageObject", "url", ORGANIZATION_LOGO);
        var organization = new HashMap<String, Object>();
        organization.put("@context", "https://schema.org");
        organization.put("@type", "Organization");
        organization.put("@id", ORGANIZATION_ID);
        organization.put("name", "Buzzing Java");
        organization.put("url", "https://buzzingjava.com/");
        organization.put("logo", logo);
        organization.put("sameAs", site.organization().sameAs());
        return writeJson(organization);
    }

    private static String bookJson(SiteProperties site) {
        var book = new java.util.HashMap<String, Object>();
        book.put("@context", "https://schema.org");
        book.put("@type", "Book");
        book.put("name", site.book().title());
        book.put("alternateName", site.book().subtitle());
        book.put("description", site.book().description());
        if (!site.book().launchDateIso().isBlank()) book.put("datePublished", site.book().launchDateIso());
        book.put("author", Map.of("@type", "Person", "name", site.book().author()));
        book.put("inLanguage", site.book().language());
        book.put("genre", site.book().genre());
        return writeJson(book);
    }

    public static String articleJson(SiteProperties site, String canonicalUrl, String headline, String description) {
        var article = new HashMap<String, Object>();
        article.put("@context", "https://schema.org");
        article.put("@type", "Article");
        article.put("@id", canonicalUrl + "#article");
        article.put("mainEntityOfPage", canonicalUrl);
        article.put("headline", headline);
        article.put("description", description);
        article.put("image", "https://buzzingjava.com/images/book-cover-hero.webp");
        article.put("author", Map.of("@type", "Person", "name", site.book().author()));
        return writeJson(article);
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
            return OBJECT_MAPPER.writeValueAsString(value)
                    .replace("<", "\\u003c")
                    .replace(">", "\\u003e")
                    .replace("&", "\\u0026");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to create structured data", exception);
        }
    }
}
