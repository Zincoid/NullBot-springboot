package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlRenderer {

    private static final TemplateEngine ENGINE;

    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        ENGINE = new TemplateEngine();
        ENGINE.setTemplateResolver(resolver);
    }

    private final Chrome chrome;
    private final ResourceLoader resourceLoader;

    public Template load(String resourcePath) {
        try {
            return new Template(Files.readString(resourceLoader.getCache(resourcePath)));
        } catch (Exception e) {
            throw new RuntimeException("模板加载失败: " + resourcePath, e);
        }
    }

    public class Template {

        private final String html;
        private final Map<String, Object> ctx = new HashMap<>();

        private Template(String html) { this.html = html; }

        public Template set(String key, String value) { ctx.put(key, value); return this; }

        public Template image(String key, String filePath) {
            File f = new File(filePath);
            if (!f.exists()) throw new RuntimeException("图片文件不存在: " + filePath);
            ctx.put(key, "file://" + f.getAbsolutePath().replace("\\", "/"));
            return this;
        }

        public Template resource(String key, String resourcePath) {
            Path p = resourceLoader.getCache(resourcePath);
            return image(key, p.toAbsolutePath().toString());
        }

        /** 渲染 HTML 模板 → 元素截图 */
        public String render(String cssSelector) throws Exception {
            Context context = new Context();
            context.setVariables(ctx);
            String resolved = ENGINE.process(html, context);

            WebDriver driver = chrome.create("3840,2160");
            try {
                Path tmp = Files.createTempFile("render-", ".html");
                try {
                    Files.writeString(tmp, resolved);
                    driver.get("file://" + tmp.toAbsolutePath());
                    chrome.ready(driver);
                    return chrome.capture(driver, cssSelector);
                } finally {
                    Files.deleteIfExists(tmp);
                }
            } finally {
                driver.quit();
            }
        }
    }
}
