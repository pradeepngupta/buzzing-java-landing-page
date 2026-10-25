package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.profiles.active=export",
    "buzzing-java.api-base=https://api.buzzingjava.com"
})
@ActiveProfiles({"test", "export"})
class StaticSiteExporterTests {
    @Test
    void exportsStandaloneSite() throws Exception {
        String ogImageAlt = "Java's runtime contracts are the check your AI-generated code still has to pass. Discover why three decades of design discipline outlast every code-generation trend. Join the Buzzing Java book launch waitlist.";
        String ogImageAltTag = "<meta property=\"og:image:alt\" content=\""
            + ogImageAlt.replace("'", "&#39;") + "\">";
        assertThat(Files.exists(Path.of("dist/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/privacypolicy/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/termsofservice/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/css/site.css"))).isTrue();
        assertThat(Files.exists(Path.of("dist/js/site.js"))).isTrue();
        assertThat(Files.exists(Path.of("dist/robots.txt"))).isTrue();
        assertThat(Files.exists(Path.of("dist/sitemap.xml"))).isTrue();
        assertThat(Files.exists(Path.of("dist/llms.txt"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/og-image.jpg"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/author-photo-192.webp"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/book-cover-hero.webp"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/book-cover-hero-700.webp"))).isTrue();
        assertThat(Files.exists(Path.of("dist/assets/web-app-manifest-512x512.png"))).isTrue();
        assertThat(Files.readString(Path.of("dist/mfwd7h3z8tbd69h3nzcnugkqk2hqq7av.txt")))
            .isEqualTo("mfwd7h3z8tbd69h3nzcnugkqk2hqq7av");
        assertThat(Files.readString(Path.of("dist/robots.txt")))
            .contains("User-agent: AhrefsBot", "Allow: /");
        assertThat(Files.readString(Path.of("dist/llms.txt")))
            .contains("# Buzzing Java", "https://buzzingjava.com/",
                "https://buzzingjava.com/book-preview/",
                "https://buzzingjava.com/privacypolicy/",
                "https://buzzingjava.com/termsofservice/");
        String homepage = Files.readString(Path.of("dist/index.html"));
        assertThat(homepage)
            .contains("Buzzing Java", "FAQPage", "css/site.css", "js/site.js",
                "<link rel=\"canonical\" href=\"https://buzzingjava.com/\" />",
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/\" />",
                "<meta property=\"og:type\" content=\"website\" />",
                ogImageAltTag,
                "Discover why Java&#39;s proven design and runtime contracts matter for trusted software in the AI era. Join the Buzzing Java book launch waitlist for updates.",
                "https://api.buzzingjava.com", "<base href=\"./\">",
                "eyebrow\">Coming Soon", "countdown-label\">Coming Soon</p>",
                "launch-state\">Coming Soon. Join the waitlist for updates</p>",
                "book-cover-hero-700.webp 700w", "loading=\"lazy\" decoding=\"async\"",
                "author-photo-192.webp", "width=\"96\" height=\"96\"")
            .doesNotContain("2026-12-07", "datePublished", "\"@type\":\"Event\"")
            .doesNotContain("data-book-reader");
        List<JsonNode> homepageEntities = structuredData(homepage);
        assertThat(homepageEntities).noneMatch(node -> node.path("@type").asText().equals("Article"));
        List<JsonNode> organizations = homepageEntities.stream()
            .filter(node -> node.path("@type").asText().equals("Organization"))
            .toList();
        assertThat(organizations).hasSize(1);
        assertThat(organizations.get(0).path("name").asText()).isEqualTo("Buzzing Java");
        assertThat(organizations.get(0).path("@id").asText())
            .isEqualTo("https://buzzingjava.com/#organization");
        assertThat(organizations.get(0).path("url").asText()).isEqualTo("https://buzzingjava.com/");
        assertThat(organizations.get(0).path("logo").path("url").asText())
            .isEqualTo("https://buzzingjava.com/assets/web-app-manifest-512x512.png");
        assertThat(organizations.get(0).path("sameAs")).hasSize(0);
        assertThat(Files.readString(Path.of("dist/privacypolicy/index.html")))
            .contains("<base href=\"../\">", "css/site.css", "href=\"./#waitlist\"",
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/privacypolicy/\">",
                ogImageAltTag,
                "Learn what information the Buzzing Java waitlist collects, how it is used and shared, how long it is retained, and how to request correction or deletion.");
        assertThat(Files.readString(Path.of("dist/termsofservice/index.html")))
            .contains("href=\"./#waitlist\"",
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/termsofservice/\">",
                ogImageAltTag,
                "Review the terms for the Buzzing Java website and waitlist, including email consent, early access, site content, and how terms may change before joining.");
        assertThat(Files.exists(Path.of("dist/book-preview/index.html"))).isTrue();
        String bookPreview = Files.readString(Path.of("dist/book-preview/index.html"));
        assertThat(bookPreview)
            .contains("Java’s Enduring Buzz", "Spread 1 of 6", "href=\"../#waitlist\"",
                "name=\"robots\" content=\"index, follow\"",
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/book-preview/\">",
                "<meta property=\"og:title\" content=\"Buzzing Java: Free Sample Chapter on Why Java Endures\">",
                "<meta property=\"og:type\" content=\"article\">",
                ogImageAltTag,
                "<h2>Java’s Enduring Buzz</h2>",
                "rel=\"canonical\" href=\"https://buzzingjava.com/book-preview/\"",
                "Buzzing Java: Free Sample Chapter on Why Java Endures",
                "<meta name=\"twitter:title\" content=\"Buzzing Java: Free Sample Chapter on Why Java Endures\">",
                "<meta name=\"twitter:card\" content=\"summary_large_image\">",
                "Read the free sample chapter of Buzzing Java: why Java&#39;s 11 buzzwords are engineering contracts that make software last. Coming Soon.")
            .doesNotContain("noindex", "<h1>Java’s Enduring Buzz</h1>")
            .doesNotContain("sample-chapter.md", "sample-chapter-source.md");
        assertThat(bookPreview.split("<h1", -1)).hasSize(2);
        List<JsonNode> previewEntities = structuredData(bookPreview);
        List<JsonNode> previewArticles = previewEntities.stream()
            .filter(node -> node.path("@type").asText().equals("Article"))
            .toList();
        assertThat(previewArticles).hasSize(1);
        JsonNode article = previewArticles.get(0);
        assertThat(article.path("@context").asText()).isEqualTo("https://schema.org");
        assertThat(article.path("@id").asText()).isEqualTo("https://buzzingjava.com/book-preview/#article");
        assertThat(article.path("mainEntityOfPage").asText()).isEqualTo("https://buzzingjava.com/book-preview/");
        assertThat(article.path("headline").asText()).isEqualTo("Java’s Enduring Buzz");
        assertThat(article.path("description").asText())
            .isEqualTo("Read the free sample chapter of Buzzing Java: why Java's 11 buzzwords are engineering contracts that make software last. Coming Soon.");
        assertThat(article.path("image").asText()).isEqualTo("https://buzzingjava.com/images/book-cover-hero.webp");
        assertThat(article.path("author").path("@type").asText()).isEqualTo("Person");
        assertThat(article.path("author").path("name").asText()).isEqualTo("Pradeep Gupta");
        assertThat(article.has("publisher")).isFalse();
        assertThat(article.has("datePublished")).isFalse();
        assertThat(article.has("dateModified")).isFalse();
        assertThat(previewEntities).noneMatch(node -> node.path("@type").asText().equals("Organization"));
        assertThat(Files.exists(Path.of("dist/book-preview/change-is-the-only-constant/index.html"))).isTrue();
        String secondPreview = Files.readString(Path.of("dist/book-preview/change-is-the-only-constant/index.html"));
        assertThat(secondPreview)
            .contains("noindex, nofollow", "Change Is the Only Constant", "<pre><code>", "FeatureFlags", "Spread 1 of 11",
                "href=\"../../#waitlist\"",
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/book-preview/change-is-the-only-constant\">")
            .doesNotContain("sample-chapter.md", "sample-chapter-source.md");
        assertThat(structuredData(secondPreview)).noneMatch(node -> node.path("@type").asText().equals("Article"));
        String sitemap = Files.readString(Path.of("dist/sitemap.xml"));
        assertThat(sitemap.lines()
            .filter(line -> line.contains("<loc>"))
            .map(String::trim)
            .toList())
            .isNotEmpty()
            .allSatisfy(location -> assertThat(location)
                .startsWith("<loc>https://buzzingjava.com/")
                .endsWith("</loc>"));
        assertThat(sitemap)
            .contains("<loc>https://buzzingjava.com/</loc>", "https://buzzingjava.com/book-preview/")
            .doesNotContain("change-is-the-only-constant", "sample-chapter-2", "analytics.buzzingjava.com");
        assertThat(Files.exists(Path.of("dist/sample-chapter-2/index.html"))).isTrue();
        assertThat(Files.readString(Path.of("dist/sample-chapter-2/index.html")))
            .contains("sample_chapter_2_visit", "change-is-the-only-constant", ogImageAltTag,
                "<meta property=\"og:url\" content=\"https://buzzingjava.com/sample-chapter-2/\">");

        assertSocialTags(Files.readString(Path.of("dist/index.html")));
        assertSocialTags(Files.readString(Path.of("dist/book-preview/index.html")));
        assertSocialTags(Files.readString(Path.of("dist/book-preview/change-is-the-only-constant/index.html")));
        assertSocialTags(Files.readString(Path.of("dist/privacypolicy/index.html")));
        assertSocialTags(Files.readString(Path.of("dist/termsofservice/index.html")));
        assertSocialTags(Files.readString(Path.of("dist/sample-chapter-2/index.html")));
    }

    private void assertSocialTags(String html) {
        assertThat(html)
            .contains(
                "property=\"og:title\" content=",
                "property=\"og:description\" content=",
                "property=\"og:url\" content=",
                "property=\"og:image\" content=",
                "property=\"og:image:alt\" content=",
                "property=\"og:image:width\" content=\"1200\"",
                "property=\"og:image:height\" content=\"630\"",
                "property=\"og:image:type\" content=",
                "property=\"og:type\" content=",
                "property=\"og:site_name\" content=",
                "property=\"og:locale\" content=",
                "name=\"twitter:card\" content=",
                "name=\"twitter:title\" content=",
                "name=\"twitter:description\" content=",
                "name=\"twitter:image\" content=",
                "name=\"twitter:image:alt\" content=",
                "name=\"twitter:site\" content=")
            .doesNotContain("content=\"\"");
    }

    private List<JsonNode> structuredData(String html) throws IOException {
        Matcher matcher = Pattern.compile(
            "<script type=\"application/ld\\+json\">(.*?)</script>", Pattern.DOTALL)
            .matcher(html);
        List<JsonNode> entities = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        while (matcher.find()) {
            entities.add(mapper.readTree(matcher.group(1)));
        }
        return entities;
    }
}
