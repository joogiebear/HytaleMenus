package com.joogiebear.hytalemenus.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a text content page.
 */
public class PageConfig {

    private String title = "Page";
    private List<String> lines = new ArrayList<>();

    public PageConfig() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    /**
     * Get all lines joined as a single string with newlines.
     */
    public String getContent() {
        return String.join("\n", lines);
    }
}
