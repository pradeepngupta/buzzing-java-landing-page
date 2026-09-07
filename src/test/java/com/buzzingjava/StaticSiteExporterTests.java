package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
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
        assertThat(Files.exists(Path.of("dist/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/privacypolicy/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/termsofservice/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/css/site.css"))).isTrue();
        assertThat(Files.exists(Path.of("dist/js/site.js"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/og-image.jpg"))).isTrue();
        assertThat(Files.readString(Path.of("dist/index.html")))
            .contains("Buzzing Java", "FAQPage", "css/site.css", "js/site.js",
                "https://api.buzzingjava.com", "<base href=\"./\">");
        assertThat(Files.readString(Path.of("dist/privacypolicy/index.html")))
            .contains("<base href=\"../\">", "css/site.css");
    }
}
