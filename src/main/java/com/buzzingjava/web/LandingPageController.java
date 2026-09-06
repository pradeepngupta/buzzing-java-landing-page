package com.buzzingjava.web;

import com.buzzingjava.config.SiteProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {
    private final SiteProperties properties;

    public LandingPageController(SiteProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/")
    public String index(Model model) {
        PageModel.addTo(model, properties);
        return "index";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        PageModel.addTo(model, properties);
        return "privacy-policy";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService(Model model) {
        PageModel.addTo(model, properties);
        return "terms-of-service";
    }
}
