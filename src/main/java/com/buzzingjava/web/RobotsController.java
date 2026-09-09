package com.buzzingjava.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotsController {
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String robots(HttpServletRequest request) {
        if ("api.buzzingjava.com".equalsIgnoreCase(request.getServerName())) {
            return "User-agent: *\nDisallow: /\n";
        }
        return "User-agent: *\nAllow: /\n\nSitemap: https://buzzingjava.com/sitemap.xml\n";
    }
}