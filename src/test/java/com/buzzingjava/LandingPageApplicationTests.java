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
import java.util.List;
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
    void rendersLandingPageAndStructuredData() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Buzzing Java")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Join the Waitlist")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FAQPage")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"@type\":\"Place\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"name\":\"Bengaluru, India\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Launch event: <span class=\"launch-location\">Bengaluru, India</span>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Be there when the story starts buzzing.")));
    }

    @Test
    void loadsConfigurationAndCounterRule() {
        assertThat(site.book().launchDateIso()).isEqualTo("2026-12-07T00:00:00+05:30");
        assertThat(site.cta().mode()).isEqualTo("waitlist");
        assertThat(site.freebie().enabled()).isFalse();
        assertThat(site.faq()).hasSize(7);
        assertThat(site.launchEvent().enabled()).isTrue();
        assertThat(site.launchEvent().location()).isEqualTo("Bengaluru, India");
    }

        @Test
        void waitlistApiReturnsRealCountAndSuccessMessage() throws Exception {
        mockMvc.perform(get("/api/waitlist/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));

        String request = "{" +
            "\"name\":\"Test Builder\",\"email\":\"builder@example.com\",\"party\":\"Online\"," +
            "\"expectations\":[\"Java's longevity secrets\",\"System design depth\"]," +
            "\"otherExpectation\":\"\"}";
        mockMvc.perform(post("/api/waitlist")
                .contentType("application/json")
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("You are on the list!"));
        }

    @Test
    void waitlistApiAcceptsMissingOptionalExpectations() throws Exception {
        String request = "{" +
            "\"name\":\"Optional Builder\",\"email\":\"optional@example.com\",\"party\":\"Online\"}";
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
                "Java longevity, AI tooling"));

        assertThat(row).hasSize(11);
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
