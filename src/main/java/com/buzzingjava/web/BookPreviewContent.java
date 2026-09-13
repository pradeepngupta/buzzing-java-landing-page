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
    private static final Pattern CODE_BLOCK = Pattern.compile("(?s)```[^\\r\\n]*\\R(.*?)```");

    public List<PreviewPage> pages() throws IOException {
        return pages("content/sample-chapter.md", PAGE_COUNT, Integer.MAX_VALUE);
    }

    public List<PreviewPage> pages(String resourcePath, int pageCount, int blockLimit) throws IOException {
        String markdown;
        try (var input = new ClassPathResource(resourcePath).getInputStream()) {
            markdown = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> codeBlocks = new ArrayList<>();
        Matcher codeMatcher = CODE_BLOCK.matcher(markdown);
        StringBuffer markdownWithPlaceholders = new StringBuffer();
        while (codeMatcher.find()) {
            codeBlocks.add(codeMatcher.group(1));
            codeMatcher.appendReplacement(markdownWithPlaceholders, Matcher.quoteReplacement("\n\n@@CODE_BLOCK_" + (codeBlocks.size() - 1) + "@@\n\n"));
        }
        codeMatcher.appendTail(markdownWithPlaceholders);
        List<String> blocks = List.of(markdownWithPlaceholders.toString().trim().split("\\R\\s*\\R"));
        if (blockLimit < blocks.size()) {
            blocks = blocks.subList(0, blockLimit);
        }
        List<PreviewPage> pages = new ArrayList<>();
        int blockCount = blocks.size();
        for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
            int start = pageNumber * blockCount / pageCount;
            int end = (pageNumber + 1) * blockCount / pageCount;
            StringBuilder html = new StringBuilder();
            for (int blockIndex = start; blockIndex < end; blockIndex++) {
                html.append(renderBlock(blocks.get(blockIndex), codeBlocks)).append('\n');
            }
            pages.add(new PreviewPage(pageNumber + 1, html.toString()));
        }
        return pages;
    }

    private String renderBlock(String block, List<String> codeBlocks) {
        String trimmed = block.trim();
        if (trimmed.startsWith("@@CODE_BLOCK_") && trimmed.endsWith("@@")) {
            int codeIndex = Integer.parseInt(trimmed.substring("@@CODE_BLOCK_".length(), trimmed.length() - 2));
            return "<pre><code>" + escapeCode(codeBlocks.get(codeIndex)) + "</code></pre>";
        }
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

    private String escapeCode(String value) {
        String code = value.endsWith("\n") ? value.substring(0, value.length() - 1) : value;
        return code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public record PreviewPage(int number, String html) {
    }
}
