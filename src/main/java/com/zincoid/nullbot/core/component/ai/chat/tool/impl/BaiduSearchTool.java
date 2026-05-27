package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BaiduSearchTool implements Tool {

    private record Args(String query) {}

    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<h3[^>]*class=\"[^\"]*(?:t|c-tit)[^\"]*\"[^>]*>\\s*<a[^>]*href=\"([^\"]+)\"[^>]*>(?:<em>)?(.*?)(?:</em>)?</a>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SNIPPET_PATTERN = Pattern.compile(
            "<(?:div|span)[^>]*class=\"[^\"]*(?:c-abstract|c-span-last|content-right_[^\"]*)[^\"]*\"[^>]*>(.*?)</(?:div|span)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final ToolDef toolDef;
    private final HttpClient httpClient;

    public BaiduSearchTool() {
        this.toolDef = ToolDef.builder("baidu_search", "百度搜索，获取最新网页信息。用于查询实时信息、新闻、百科知识等")
                .addString("query", "搜索关键词", true)
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
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

            String html = fetchSearchResults(query);
            List<SearchResult> results = parseResults(html);

            if (results.isEmpty()) {
                return "未找到与 \"" + query + "\" 相关的搜索结果";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("百度搜索 \"").append(query).append("\" 的结果:\n");
            for (int i = 0; i < results.size(); i++) {
                SearchResult r = results.get(i);
                sb.append(i + 1).append(". ").append(r.title).append("\n");
                sb.append("链接: ").append(r.url).append("\n");
                if (!r.snippet.isEmpty()) {
                    sb.append("摘要: ").append(r.snippet).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("◉ [BaiduSearchTool] 搜索失败: {}", e.getMessage());
            return "错误: 百度搜索失败 - " + e.getMessage();
        }
    }

    private String fetchSearchResults(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.baidu.com/s?wd=" + encodedQuery;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private List<SearchResult> parseResults(String html) {
        List<SearchResult> results = new ArrayList<>();
        Matcher matcher = TITLE_PATTERN.matcher(html);
        int count = 0;

        while (matcher.find() && count < 10) {
            String url = matcher.group(1);
            String title = TAG_PATTERN.matcher(matcher.group(2)).replaceAll("").trim();
            if (title.isEmpty()) continue;

            String snippet = "";
            int searchStart = matcher.end();
            Matcher snippetMatcher = SNIPPET_PATTERN.matcher(html);
            if (snippetMatcher.find(searchStart) && snippetMatcher.start() - searchStart < 800) {
                snippet = TAG_PATTERN.matcher(snippetMatcher.group(1)).replaceAll("").trim();
                snippet = snippet.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
            }

            results.add(new SearchResult(title, url, snippet));
            count++;
        }

        return results;
    }

    private record SearchResult(String title, String url, String snippet) {
    }
}
