package com.buzzingjava;

import com.buzzingjava.config.SiteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SiteProperties.class)
public class BuzzingJavaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuzzingJavaApplication.class, args);
    }
}
