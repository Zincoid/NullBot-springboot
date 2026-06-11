package com.zincoid.nullbot.core.module.ai.chat.tool.impl;

import com.zincoid.nullbot.core.module.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@Component
public class BaiduSearchTool implements Tool {

    private record Args(String query) {}

    // 匹配标题所在整个h3块（class包含c-title）
    private static final Pattern TITLE_BLOCK_PATTERN = Pattern.compile(
            "<h3[^>]*class=\"[^\"]*\\bc-title\\b[^\"]*\"[^>]*>\\s*<a[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );

    // 尝试匹配多种摘要容器（按优先级排列）
    private static final Pattern[] SNIPPET_PATTERNS = {
            Pattern.compile("<div[^>]*class=\"[^\"]*\\bc-font-normal\\b[^\"]*\\bc-color-text\\b[^\"]*\"[^>]*>\\s*<div[^>]*class=\"[^\"]*\\btext_\\w+\\b[^\"]*\"[^>]*>(.*?)</div>\\s*</div>", Pattern.DOTALL),
            Pattern.compile("<div[^>]*class=\"[^\"]*\\bc-abstract\\b[^\"]*\"[^>]*>(.*?)</div>", Pattern.DOTALL),
            Pattern.compile("<div[^>]*class=\"[^\"]*\\bc-span-last\\b[^\"]*\"[^>]*>(.*?)</div>", Pattern.DOTALL),
            Pattern.compile("<div[^>]*class=\"[^\"]*\\bcontent-right_\\w+\\b[^\"]*\"[^>]*>(.*?)</div>", Pattern.DOTALL),
            // 兜底：任何包含"摘要"特征但不含广告标记的div
            Pattern.compile("<div[^>]*class=\"[^\"]*(?:abstract|summary|desc|content)[^\"]*\"[^>]*>(.*?)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
    };

    // 去除HTML标签
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");

    private final ToolDef toolDef;
    private final HttpClient httpClient;

    public BaiduSearchTool() {
        this.toolDef = ToolDef.builder("baidu_search", "百度搜索，用于查询实时信息、新闻、百科等。" +
                        "此工具易被安全检查导致搜索不到结果，此时需使用其他搜索工具。")
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
        Matcher titleMatcher = TITLE_BLOCK_PATTERN.matcher(html);
        int count = 0;

        // 记录每个标题的结束位置，用于后续匹配摘要
        List<MatchPosition> titlePositions = new ArrayList<>();
        while (titleMatcher.find() && count < 10) {
            String url = titleMatcher.group(1);
            // 过滤掉百度内部广告、推荐链接（可根据url特征过滤）
            if (isInvalidUrl(url)) {
                continue;
            }
            String rawTitle = titleMatcher.group(2);
            String title = stripHtmlTags(rawTitle).trim();
            if (title.isEmpty()) continue;

            int titleEnd = titleMatcher.end();
            titlePositions.add(new MatchPosition(titleEnd, title, url));
            count++;
        }

        // 为每条结果寻找最近的摘要（在标题之后800字符内出现）
        for (MatchPosition pos : titlePositions) {
            String snippet = findSnippetAfterPosition(html, pos.endPos);
            results.add(new SearchResult(pos.title, pos.url, snippet));
        }

        return results;
    }

    private String findSnippetAfterPosition(String html, int startPos) {
        String subHtml = html.substring(startPos, Math.min(startPos + 2000, html.length()));
        for (Pattern pattern : SNIPPET_PATTERNS) {
            Matcher matcher = pattern.matcher(subHtml);
            if (matcher.find()) {
                String rawSnippet = matcher.group(1);
                String cleaned = stripHtmlTags(rawSnippet);
                cleaned = decodeHtmlEntities(cleaned);
                if (cleaned.length() > 300) {
                    cleaned = cleaned.substring(0, 300) + "...";
                }
                return cleaned;
            }
        }
        return "";
    }

    private boolean isInvalidUrl(String url) {
        // 过滤百度自身广告、推荐、其他内部引导页面
        return url.contains("posid=") || url.contains("baidu.com/link?url=") && url.length() < 30
                || url.startsWith("javascript:") || url.contains("baidu.com/s?");
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        String noTags = TAG_PATTERN.matcher(html).replaceAll("");
        // 合并多余空白
        return noTags.replaceAll("\\s+", " ").trim();
    }

    private String decodeHtmlEntities(String text) {
        return text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    private record MatchPosition(int endPos, String title, String url) {}
    private record SearchResult(String title, String url, String snippet) {}
}