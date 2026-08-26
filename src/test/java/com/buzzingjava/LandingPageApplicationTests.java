package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.buzzingjava.config.SiteProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
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
        assertThat(site.waitlist().counter().currentCount()).isGreaterThan(site.waitlist().counter().threshold());
        assertThat(site.freebie().enabled()).isFalse();
        assertThat(site.faq()).hasSize(7);
        assertThat(site.launchEvent().enabled()).isTrue();
        assertThat(site.launchEvent().location()).isEqualTo("Bengaluru, India");
    }

        @Test
        void waitlistApiReturnsAndIncrementsServerCount() throws Exception {
        int initialCount = site.waitlist().counter().currentCount();
        mockMvc.perform(get("/api/waitlist/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(initialCount));

        String request = "{" +
            "\"name\":\"Test Builder\",\"email\":\"builder@example.com\",\"party\":\"Online\"," +
            "\"expectations\":[\"Java's longevity secrets\",\"System design depth\"]," +
            "\"otherExpectation\":\"\"}";
        mockMvc.perform(post("/api/waitlist")
                .contentType("application/json")
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.is(initialCount + 1),
                org.hamcrest.Matchers.is(initialCount + 3),
                org.hamcrest.Matchers.is(initialCount + 5),
                org.hamcrest.Matchers.is(initialCount + 10))));
        }
}
