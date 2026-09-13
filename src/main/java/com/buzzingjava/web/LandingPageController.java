package com.buzzingjava.web;

import com.buzzingjava.config.SiteProperties;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {
    private final SiteProperties properties;
    private final BookPreviewContent bookPreviewContent;

    public LandingPageController(SiteProperties properties, BookPreviewContent bookPreviewContent) {
        this.properties = properties;
        this.bookPreviewContent = bookPreviewContent;
    }

    @GetMapping("/")
    public String index(Model model) {
        PageModel.addTo(model, properties);
        return "index";
    }

    @GetMapping({"/book-preview", "/book-preview/"})
    public String bookPreview(Model model) throws IOException {
        PageModel.addTo(model, properties);
        model.addAttribute("previewPages", bookPreviewContent.pages());
        return "book-preview";
    }

    @GetMapping({"/privacy-policy", "/privacy-policy/", "/privacypolicy", "/privacypolicy/"})
    public String privacyPolicy(Model model) {
        PageModel.addTo(model, properties);
        return "privacy-policy";
    }

    @GetMapping({"/terms-of-service", "/terms-of-service/", "/termsofservice", "/termsofservice/"})
    public String termsOfService(Model model) {
        PageModel.addTo(model, properties);
        return "terms-of-service";
    }
}
