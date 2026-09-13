package com.buzzingjava.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BookPreviewContent {
    private static final int PAGE_COUNT = 11;
    private static final Pattern HEADING = Pattern.compile("^(#{1,3})\\s+(.+)$");

    public List<PreviewPage> pages() throws IOException {
        String markdown;
        try (var input = new ClassPathResource("content/sample-chapter.md").getInputStream()) {
            markdown = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> blocks = List.of(markdown.trim().split("\\R\\s*\\R"));
        List<PreviewPage> pages = new ArrayList<>();
        int blockCount = blocks.size();
        for (int pageNumber = 0; pageNumber < PAGE_COUNT; pageNumber++) {
            int start = pageNumber * blockCount / PAGE_COUNT;
            int end = (pageNumber + 1) * blockCount / PAGE_COUNT;
            StringBuilder html = new StringBuilder();
            for (int blockIndex = start; blockIndex < end; blockIndex++) {
                html.append(renderBlock(blocks.get(blockIndex))).append('\n');
            }
            pages.add(new PreviewPage(pageNumber + 1, html.toString()));
        }
        return pages;
    }

    private String renderBlock(String block) {
        String trimmed = block.trim();
        Matcher heading = HEADING.matcher(trimmed);
        if (heading.matches()) {
            int level = heading.group(1).length();
            return "<h" + level + ">" + inline(heading.group(2)) + "</h" + level + ">";
        }
        if (trimmed.startsWith("· ") || trimmed.startsWith("- ")) {
            StringBuilder list = new StringBuilder("<ul>");
            for (String line : trimmed.split("\\R")) {
                String item = line.trim();
                if (item.startsWith("· ") || item.startsWith("- ")) {
                    list.append("<li>").append(inline(item.substring(2))).append("</li>");
                }
            }
            return list.append("</ul>").toString();
        }
        if (trimmed.startsWith("*") && trimmed.endsWith("*")) {
            return "<p class=\"preview-subtitle\">" + inline(trimmed) + "</p>";
        }
        return "<p>" + inline(trimmed.replaceAll("\\R+", " ")) + "</p>";
    }

    private String inline(String value) {
        String escaped = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        escaped = escaped.replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1");
        escaped = escaped.replaceAll("\\*\\*([^*]+)\\*\\*", "<strong>$1</strong>");
        escaped = escaped.replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "<em>$1</em>");
        return escaped;
    }

    public record PreviewPage(int number, String html) {
    }
}
