package com.zincoid.nullbot.core.component.render.browser;

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

    private static final TemplateEngine TEMPLATE_ENGINE;

    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(resolver);
    }

    private final Chrome chrome;
    private final ResourceLoader resourceLoader;

    // =========================== 载入方法 ===========================

    public Template load(String resourcePath) {
        try {
            return new Template(Files.readString(
                    resourceLoader.getCache(resourcePath)));
        } catch (Exception e) {
            throw new RuntimeException("模板加载失败: " + resourcePath, e);
        }
    }

    public class Template {

        private final String html;
        private final Map<String, Object> ctx = new HashMap<>();

        private Template(String html) { this.html = html; }

        // ============================ 构建方法 ===========================

        public Template string(String key, String value) {
            ctx.put(key, value);
            return this;
        }
        public Template file(String key, String path) {
            File f = new File(path);
            if (!f.exists()) throw new RuntimeException("文件不存在: " + path);
            ctx.put(key, "file://" +
                    f.getAbsolutePath().replace("\\", "/"));
            return this;
        }
        public Template resource(String key, String path) {
            Path p = resourceLoader.getCache(path);
            return file(key, p.toAbsolutePath().toString());
        }

        // ============================= 渲染方法 ===========================

        public String render(String cssSelector) throws Exception {
            Context context = new Context();
            context.setVariables(ctx);
            String resolved = TEMPLATE_ENGINE.process(html, context);
            WebDriver driver = null;
            Path tmp = null;
            try {
                driver = chrome.create("3840,2160");
                tmp = Files.createTempFile("render-", ".html");
                Files.writeString(tmp, resolved);
                driver.get("file://" + tmp.toAbsolutePath());
                chrome.ready(driver);
                return chrome.capture(driver, cssSelector);
            } finally {
                if (tmp != null) Files.deleteIfExists(tmp);
                if (driver != null) driver.quit();
            }
        }
    }
}
