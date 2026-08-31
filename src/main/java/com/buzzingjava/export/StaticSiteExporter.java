package com.buzzingjava.export;

import com.buzzingjava.config.SiteProperties;
import com.buzzingjava.web.PageModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@Profile("export")
public class StaticSiteExporter implements CommandLineRunner {
    private final SiteProperties site;
    private final TemplateEngine templateEngine;

    public StaticSiteExporter(SiteProperties site, TemplateEngine templateEngine) {
        this.site = site;
        this.templateEngine = templateEngine;
    }

    @Override
    public void run(String... args) throws IOException {
        Path output = Path.of("dist");
        Files.createDirectories(output);
        var model = new org.springframework.ui.ExtendedModelMap();
        PageModel.addTo(model, site);
        var context = new Context(Locale.ENGLISH, model.asMap());
        Files.writeString(output.resolve("index.html"), templateEngine.process("index", context));
        copyResource("static/css/site.css", output.resolve("css/site.css"));
        copyResource("static/js/site.js", output.resolve("js/site.js"));
        copyResource("static/images/og-image.jpg", output.resolve("images/og-image.jpg"));
        copyResource("static/images/author-photo.jpg", output.resolve("images/author-photo.jpg"));
        copyResource("static/images/book-cover-hero.webp", output.resolve("images/book-cover-hero.webp"));
        copyResource("static/assets/apple-touch-icon.png", output.resolve("assets/apple-touch-icon.png"));
        copyResource("static/assets/favicon-96x96.png", output.resolve("assets/favicon-96x96.png"));
        copyResource("static/assets/favicon.svg", output.resolve("assets/favicon.svg"));
        copyResource("static/assets/favicon.ico", output.resolve("assets/favicon.ico"));
        copyResource("static/assets/site.webmanifest", output.resolve("assets/site.webmanifest"));
        copyResource("static/assets/web-app-manifest-192x192.png", output.resolve("assets/web-app-manifest-192x192.png"));
        copyResource("static/assets/web-app-manifest-512x512.png", output.resolve("assets/web-app-manifest-512x512.png"));

        Files.createDirectories(output.resolve("fonts"));
    }

    private void copyResource(String classpath, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (var input = new ClassPathResource(classpath).getInputStream()) {
            Files.copy(input, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
