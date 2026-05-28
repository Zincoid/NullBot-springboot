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
public class BingSearchTool implements Tool {

    private record Args(String query) {}

    // 匹配每个结果区块（b_algo）
    private static final Pattern RESULT_BLOCK_PATTERN = Pattern.compile(
            "<li[^>]*class=\"[^\"]*\\bb_algo\\b[^\"]*\"[^>]*>(.*?)</li>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // 匹配标题（在h2下的a）
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<h2[^>]*>\\s*<a[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>\\s*</h2>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // 匹配摘要（b_caption下的p或直接文本）
    private static final Pattern SNIPPET_PATTERN = Pattern.compile(
            "<div[^>]*class=\"[^\"]*\\bb_caption\\b[^\"]*\"[^>]*>\\s*(?:<p[^>]*>(.*?)</p>|(.*?))\\s*</div>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // 清理HTML标签
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    // 清理摘要前的日期/时间前缀（如 "2 天之前 · "）
    private static final Pattern DATE_PREFIX_PATTERN = Pattern.compile("^\\d+\\s+[天周月年]前\\s*[·•]?\\s*");

    private final ToolDef toolDef;
    private final HttpClient httpClient;

    public BingSearchTool() {
        this.toolDef = ToolDef.builder("bing_search", "Bing搜索，获取最新网页信息。用于查询实时信息、新闻、百科知识等")
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

    private String fetchSearchResults(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        // 添加 &count=10 限制结果数量，提升响应速度
        String url = "https://www.bing.com/search?q=" + encodedQuery + "&count=10";

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
        Matcher blockMatcher = RESULT_BLOCK_PATTERN.matcher(html);

        while (blockMatcher.find()) {
            String block = blockMatcher.group(1);
            if (isAdBlock(block)) {
                continue; // 跳过广告推广块
            }

            // 提取标题和链接
            Matcher titleMatcher = TITLE_PATTERN.matcher(block);
            if (!titleMatcher.find()) {
                continue;
            }
            String url = titleMatcher.group(1);
            if (isInvalidUrl(url)) {
                continue;
            }
            String rawTitle = titleMatcher.group(2);
            String title = stripHtmlTags(rawTitle).trim();
            if (title.isEmpty()) {
                continue;
            }

            // 提取摘要
            String snippet = extractSnippet(block);
            results.add(new SearchResult(title, url, snippet));

            if (results.size() >= 10) break;
        }
        return results;
    }

    private String extractSnippet(String block) {
        Matcher snippetMatcher = SNIPPET_PATTERN.matcher(block);
        if (!snippetMatcher.find()) {
            return "";
        }
        // group(1) 是 <p> 内容，group(2) 是直接文本
        String rawSnippet = snippetMatcher.group(1) != null ? snippetMatcher.group(1) : snippetMatcher.group(2);
        if (rawSnippet == null) return "";

        String cleaned = stripHtmlTags(rawSnippet);
        cleaned = decodeHtmlEntities(cleaned);
        // 移除日期前缀
        cleaned = DATE_PREFIX_PATTERN.matcher(cleaned).replaceFirst("");
        // 合并多余空白并限制长度
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > 300) {
            cleaned = cleaned.substring(0, 300) + "...";
        }
        return cleaned;
    }

    private boolean isAdBlock(String block) {
        // 广告块通常包含 "b_ad" 或 "advertisement" 等标志
        return block.contains("b_ad") || block.contains("advertisement") || block.contains("data-ad");
    }

    private boolean isInvalidUrl(String url) {
        // 过滤Bing自身链接、空链接、JavaScript等
        return url == null || url.isBlank() ||
                url.startsWith("javascript:") ||
                url.contains("bing.com/aclick") ||
                url.contains("bing.com/videos") && !url.contains("?q=");  // 视频聚合页不算
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return TAG_PATTERN.matcher(html).replaceAll("");
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