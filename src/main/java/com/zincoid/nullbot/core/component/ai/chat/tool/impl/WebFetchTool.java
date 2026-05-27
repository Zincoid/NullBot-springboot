package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

@Slf4j
public class WebFetchTool implements Tool {

    private record Args(String url) {}

    private static final int MAX_BODY_LENGTH = 8192;

    private static final Pattern[] STRIP_PATTERNS = {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<style[^>]*>.*?</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<head[^>]*>.*?</head>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<!--.*?-->", Pattern.DOTALL),
            Pattern.compile("<[^>]+>")
    };

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s{3,}");

    private final ToolDef toolDef;
    private final HttpClient httpClient;

    public WebFetchTool() {
        this.toolDef = ToolDef.builder("web_fetch", "获取任意网页的文本内容，用于阅读文章、查看详情等。适合在搜索后进一步了解具体信息")
                .addString("url", "网页URL链接", true)
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
            String url = args.url();
            if (url == null || url.isBlank()) {
                return "错误: URL不能为空";
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "错误: 不支持的URL协议，仅支持 http/https";
            }

            String html = fetchPage(url);
            String text = extractText(html);

            return "网页内容 (" + url + "):\n" + text;
        } catch (Exception e) {
            log.warn("◉ [WebFetchTool] 获取失败: {}", e.getMessage());
            return "错误: 获取网页失败 - " + e.getMessage();
        }
    }

    private String fetchPage(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String contentType = response.headers().firstValue("Content-Type").orElse("");
        if (!contentType.contains("text/html") && !contentType.contains("text/plain")) {
            throw new RuntimeException("不支持的Content-Type: " + contentType + "，仅支持HTML/文本页面");
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        return response.body();
    }

    private String extractText(String html) {
        String text = html;
        for (Pattern p : STRIP_PATTERNS) {
            text = p.matcher(text).replaceAll("");
        }

        text = text.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        text = WHITESPACE_PATTERN.matcher(text).replaceAll("\n").trim();

        if (text.length() > MAX_BODY_LENGTH) {
            text = text.substring(0, MAX_BODY_LENGTH) + "...(内容已截断)";
        }
        return text;
    }
}
