package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.buzzingjava.config.SiteProperties;
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
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Launch event: <span>Bengaluru, India</span>")))
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
    void waitlistApiAllowsGitHubPagesOrigin() throws Exception {
        mockMvc.perform(options("/api/waitlist")
                .header("Origin", "https://pradeepngupta.github.io")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Access-Control-Allow-Origin", "https://pradeepngupta.github.io"));
    }
}
