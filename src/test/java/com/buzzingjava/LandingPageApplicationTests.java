package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.buzzingjava.config.SiteProperties;
import com.buzzingjava.waitlist.GoogleSheetsService;
import com.buzzingjava.waitlist.WaitlistSheetRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LandingPageApplicationTests {
    @Autowired MockMvc mockMvc;
    @Autowired SiteProperties site;

    @Test
    void reportsApplicationHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void rendersLandingPageAndStructuredData() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Buzzing Java")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Get the Free Chapter + Launch Price")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("What's Inside")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sample_chapter_1_click")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/book-preview/\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Appendices")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Appendix D: Upgrade Strategy Without Panic")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java Since 1.3 → 25")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FAQPage")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Coming Soon")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("countdown-label\">Coming Soon</p>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("launch-state\">Coming Soon. Join the waitlist for updates</p>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Be there when the story starts buzzing.")));
    }

    @Test
    void rendersOneValidOrganizationEntityOnHomepage() throws Exception {
        String html = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<JsonNode> organizations = structuredData(html).stream()
                .filter(node -> node.path("@type").asText().equals("Organization"))
                .toList();

        assertThat(organizations).hasSize(1);
        JsonNode organization = organizations.get(0);
        assertThat(organization.path("@context").asText()).isEqualTo("https://schema.org");
        assertThat(organization.path("@id").asText()).isEqualTo("https://buzzingjava.com/#organization");
        assertThat(organization.path("name").asText()).isEqualTo("Buzzing Java");
        assertThat(organization.path("url").asText()).isEqualTo("https://buzzingjava.com/");
        assertThat(organization.path("logo").path("@type").asText()).isEqualTo("ImageObject");
        assertThat(organization.path("logo").path("url").asText())
                .isEqualTo("https://buzzingjava.com/assets/web-app-manifest-512x512.png");
        assertThat(organization.path("sameAs").isArray()).isTrue();
        assertThat(organization.path("sameAs")).hasSize(0);
        assertThat(html)
                .contains("<link rel=\"canonical\" href=\"https://buzzingjava.com/\" />",
                        "<meta property=\"og:title\" content=\"" + site.seo().title() + "\" />",
                        "<meta property=\"og:url\" content=\"https://buzzingjava.com/\" />",
                        "<meta name=\"twitter:title\" content=\"" + site.seo().title() + "\">",
                        "<meta name=\"twitter:card\" content=\"summary_large_image\">",
                        "<meta name=\"twitter:site\" content=\"@pradeepxgupta\" />");
        assertThat(structuredData(html)).noneMatch(node -> node.path("@type").asText().equals("Article"));
    }

    @Test
    void rendersBookPreviewReader() throws Exception {
        mockMvc.perform(get("/book-preview"))
                .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"robots\" content=\"index, follow\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("rel=\"canonical\" href=\"https://buzzingjava.com/book-preview/\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Buzzing Java: Free Sample Chapter on Why Java Endures")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Read the free sample chapter of Buzzing Java: why Java&#39;s 11 buzzwords are engineering contracts that make software last. Coming Soon.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Java’s Enduring Buzz")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-book-reader")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-reader-next")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/#waitlist\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Spread 1 of 6")));
    }

    @Test
    void keepsBookPreviewPageSpecificMetadataWithoutOrganizationEntity() throws Exception {
        String html = mockMvc.perform(get("/book-preview/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html)
                .contains("<title>Buzzing Java: Free Sample Chapter on Why Java Endures</title>",
                        "<link rel=\"canonical\" href=\"https://buzzingjava.com/book-preview/\">",
                        "<meta property=\"og:title\" content=\"Buzzing Java: Free Sample Chapter on Why Java Endures\">",
                        "<meta property=\"og:description\" content=\"Read the free sample chapter of Buzzing Java: why Java&#39;s 11 buzzwords are engineering contracts that make software last. Coming Soon.\">",
                        "<meta property=\"og:url\" content=\"https://buzzingjava.com/book-preview/\">",
                        "<meta property=\"og:type\" content=\"article\">",
                        "<meta name=\"twitter:title\" content=\"Buzzing Java: Free Sample Chapter on Why Java Endures\">",
                        "<meta name=\"twitter:description\" content=\"Read the free sample chapter of Buzzing Java: why Java&#39;s 11 buzzwords are engineering contracts that make software last. Coming Soon.\">",
                        "<meta name=\"twitter:card\" content=\"summary_large_image\">");
        List<JsonNode> entities = structuredData(html);
        List<JsonNode> articles = entities.stream()
                .filter(node -> node.path("@type").asText().equals("Article"))
                .toList();
        assertThat(articles).hasSize(1);
        JsonNode article = articles.get(0);
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
        assertThat(entities).noneMatch(node -> node.path("@type").asText().equals("Organization"));
    }

    @Test
    void rendersChangeIsTheOnlyConstantPreview() throws Exception {
        mockMvc.perform(get("/book-preview/change-is-the-only-constant"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("noindex, nofollow")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SAMPLE CHAPTER / 02")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Change Is the Only Constant")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Stability is the ability to absorb change without breaking.")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<pre><code>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("    private final Map&lt;String, Boolean&gt; flags")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Spread 1 of 11")));
    }

    @Test
    void rendersSampleChapterTwoTrackingRedirect() throws Exception {
        mockMvc.perform(get("/sample-chapter-2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sample_chapter_2_visit")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("change-is-the-only-constant")));
    }

            @Test
            void servesIndexingRulesForLandingAndApiHosts() throws Exception {
            mockMvc.perform(get("/robots.txt").header("Host", "buzzingjava.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User-agent: AhrefsBot\nAllow: /")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Allow: /")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://buzzingjava.com/sitemap.xml")));
            mockMvc.perform(get("/robots.txt").header("Host", "api.buzzingjava.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("User-agent: *\nDisallow: /\n"));
            }

            @Test
            void servesAhrefsVerificationKeyAtRoot() throws Exception {
            mockMvc.perform(get("/mfwd7h3z8tbd69h3nzcnugkqk2hqq7av.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string("mfwd7h3z8tbd69h3nzcnugkqk2hqq7av"));
            }

            @Test
            void rendersPrivacyAndTermsPages() throws Exception {
            mockMvc.perform(get("/privacy-policy"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Privacy Policy")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Kit and Brevo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("pradeep@buzzingjava.com")));
            mockMvc.perform(get("/privacypolicy/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Privacy Policy")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"./#waitlist\"")));
            mockMvc.perform(get("/terms-of-service"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Terms of Service")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Early access")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("unsubscribe")));
            mockMvc.perform(get("/termsofservice/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Terms of Service")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"./#waitlist\"")));
            }

    @Test
    void loadsConfigurationAndCounterRule() {
        assertThat(site.book().launchDate()).isEqualTo("Postponed — new date TBD");
        assertThat(site.book().launchDateIso()).isEmpty();
        assertThat(site.cta().mode()).isEqualTo("waitlist");
        assertThat(site.freebie().enabled()).isFalse();
        assertThat(site.faq()).hasSize(8);
        assertThat(site.launchEvent().enabled()).isFalse();
        assertThat(site.launchEvent().startDate()).isEmpty();
    }

    private List<JsonNode> structuredData(String html) throws Exception {
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

        @Test
        void waitlistApiReturnsRealCountAndSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/waitlist/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));

        String request = "{" +
            "\"name\":\"Test Builder\",\"email\":\"builder@example.com\",\"party\":\"Online\"," +
            "\"expectations\":[\"Java's longevity secrets\",\"System design depth\"]," +
            "\"otherExpectation\":\"\",\"privacyConsent\":true}";
        mockMvc.perform(post("/api/waitlist")
                .contentType("application/json")
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("You are on the list!"));
        }

    @Test
    void waitlistApiAcceptsMissingOptionalExpectations() throws Exception {
        String request = "{" +
            "\"name\":\"Optional Builder\",\"email\":\"optional@example.com\",\"party\":\"Online\",\"privacyConsent\":true}";
        mockMvc.perform(post("/api/waitlist")
                .contentType("application/json")
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("You are on the list!"));
    }

    @Test
    void waitlistRowUsesFixedOrderWithTimestampFallback() {
        List<Object> row = GoogleSheetsService.buildRow(new WaitlistSheetRow(
                "2026-08-29T15:21:32.123Z",
                "Ada Lovelace",
                "ada@example.com",
                "Online",
                "newsletter",
                "social",
                "launch",
                "127.0.0.1",
                "",
                "",
                "Java longevity, AI tooling",
                "true"));

            assertThat(row).hasSize(12);
        assertThat((String)row.get(0)).isNotBlank();
        assertThat(row.get(1)).isEqualTo("Ada Lovelace");
        assertThat(row.get(2)).isEqualTo("ada@example.com");
        assertThat(row.get(3)).isEqualTo("Online");
        assertThat(row.get(4)).isEqualTo("newsletter");
        assertThat(row.get(5)).isEqualTo("social");
        assertThat(row.get(6)).isEqualTo("launch");
        assertThat(row.get(7)).isEqualTo("127.0.0.1");
        assertThat(row.get(8)).isEqualTo("");
        assertThat(row.get(9)).isEqualTo("");
        assertThat(row.get(10)).isEqualTo("Java longevity, AI tooling");
        assertThat(row.get(11)).isEqualTo("true");
    }

    @Test
    void waitlistApiAllowsGitHubPagesOrigin() throws Exception {
        mockMvc.perform(options("/api/waitlist")
                .header("Origin", "https://pradeepngupta.github.io")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Access-Control-Allow-Origin", "https://pradeepngupta.github.io"));
    }
}
