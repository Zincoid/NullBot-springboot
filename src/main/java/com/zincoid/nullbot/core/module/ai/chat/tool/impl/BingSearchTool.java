package com.zincoid.nullbot.core.module.ai.chat.tool.impl;

import com.zincoid.nullbot.core.module.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BingSearchTool implements Tool {

    private record Args(String query) {}

    private final ToolDef toolDef;
    private final HttpClient httpClient;
    private final XMLInputFactory xmlInputFactory;

    public BingSearchTool() {
        this.toolDef = ToolDef.builder("bing_search", "Bing搜索，用于查询实时信息、新闻、百科等。" +
                        "此工具尽量使用简短的单个词语搜索，多词易查不到相关内容，如果查不到可缩短关键词再试。")
                .addString("query", "搜索关键词", true)
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.xmlInputFactory = XMLInputFactory.newInstance();
        // 防止 XXE
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            Args args = ToolDef.parseArgs(jsonArgs, Args.class);
            String query = args.query();
            if (query == null || query.isBlank()) {
                return "错误: 搜索关键词不能为空";
            }

            List<SearchResult> results = fetchSearchResults(query);

            if (results.isEmpty()) {
                return "未找到与 \"" + query + "\" 相关的搜索结果";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Bing搜索 \"").append(query).append("\" 的结果:\n\n");
            for (int i = 0; i < results.size(); i++) {
                SearchResult r = results.get(i);
                sb.append(i + 1).append(". ").append(r.title).append("\n");
                sb.append("   链接: ").append(r.url).append("\n");
                if (!r.snippet.isEmpty()) {
                    sb.append("   摘要: ").append(r.snippet).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("◉ [BingSearchTool] 搜索失败: {}", e.getMessage());
            return "错误: Bing搜索失败 - " + e.getMessage();
        }
    }

    private List<SearchResult> fetchSearchResults(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String url = "https://cn.bing.com/search?format=rss&q=" + encodedQuery + "&count=10";
        log.info("◎ [BingSearch] RSS请求URL: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/rss+xml, application/xml, text/xml")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        return parseRss(response.body());
    }

    private List<SearchResult> parseRss(String xml) throws Exception {
        List<SearchResult> results = new ArrayList<>();
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));

        String title = null;
        String link = null;
        StringBuilder description = new StringBuilder();
        StringBuilder currentText = new StringBuilder();
        boolean inItem = false;
        boolean inDescription = false;
        boolean inTitle = false;
        boolean inLink = false;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "item" -> {
                            inItem = true;
                            title = null;
                            link = null;
                            description.setLength(0);
                        }
                        case "title" -> {
                            if (inItem) { inTitle = true; currentText.setLength(0); }
                        }
                        case "link" -> {
                            if (inItem) { inLink = true; currentText.setLength(0); }
                        }
                        case "description" -> {
                            if (inItem) { inDescription = true; currentText.setLength(0); }
                        }
                    }
                }
                case XMLStreamConstants.CHARACTERS -> {
                    if (inTitle || inLink || inDescription) {
                        currentText.append(reader.getText());
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "title" -> {
                            if (inItem) title = currentText.toString();
                            inTitle = false;
                        }
                        case "link" -> {
                            if (inItem) link = currentText.toString();
                            inLink = false;
                        }
                        case "description" -> {
                            if (inItem) description.append(currentText);
                            inDescription = false;
                        }
                        case "item" -> {
                            if (title != null && link != null && !title.isBlank()) {
                                String snippet = stripHtmlTags(description.toString().trim());
                                snippet = decodeHtmlEntities(snippet);
                                snippet = snippet.replaceAll("\\s+", " ").trim();
                                if (snippet.length() > 300) {
                                    snippet = snippet.substring(0, 300) + "...";
                                }
                                results.add(new SearchResult(title, link, snippet));
                            }
                            inItem = false;
                            if (results.size() >= 10) return results;
                        }
                    }
                }
            }
        }
        reader.close();
        return results;
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "");
    }

    private String decodeHtmlEntities(String text) {
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }

    private record SearchResult(String title, String url, String snippet) {}
}
