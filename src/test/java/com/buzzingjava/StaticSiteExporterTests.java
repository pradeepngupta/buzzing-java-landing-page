package com.buzzingjava;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=export")
@ActiveProfiles({"test", "export"})
class StaticSiteExporterTests {
    @Test
    void exportsStandaloneSite() throws Exception {
        assertThat(Files.exists(Path.of("dist/index.html"))).isTrue();
        assertThat(Files.exists(Path.of("dist/css/site.css"))).isTrue();
        assertThat(Files.exists(Path.of("dist/js/site.js"))).isTrue();
        assertThat(Files.exists(Path.of("dist/images/og-image.svg"))).isTrue();
        assertThat(Files.readString(Path.of("dist/index.html")))
                .contains("Buzzing Java", "FAQPage", "css/site.css", "js/site.js");
    }
}
