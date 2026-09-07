package com.buzzingjava.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String[] REQUIRED_ORIGINS = {
            "https://pradeepngupta.github.io",
            "https://buzzingjava.com",
            "https://www.buzzingjava.com"
    };

    private final String[] allowedOrigins;

    public WebConfig(@Value("${WAITLIST_ALLOWED_ORIGINS:}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream((allowedOrigins + "," + String.join(",", REQUIRED_ORIGINS)).split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}