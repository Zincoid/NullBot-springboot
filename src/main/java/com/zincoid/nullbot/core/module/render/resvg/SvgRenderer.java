package com.zincoid.nullbot.core.module.render.resvg;

import com.zincoid.nullbot.core.module.resource.loader.ResourceLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SvgRenderer {

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    public Template load(String svg) {  // 载入模板
        return new Template(svg);
    }

    public class Template {

        private final String svg;
        private final Map<String, Object> ctx = new HashMap<>();

        private Template(String svg) { this.svg = svg; }

        // ================= 构建方法 =================

        public Template string(String key, String value) {
            ctx.put(key, value);
            return this;
        }
        public Template number(String key, Object value) {
            ctx.put(key, value);
            return this;
        }
        public Template image(String key, String path, boolean gray) {
            ctx.put(key, Resvg.toImgUri(path, gray));
            return this;
        }
        public Template resource(String key, String path, boolean gray) {
            Path p = resourceLoader.getCache(path);
            return image(key, p.toAbsolutePath().toString(), gray);
        }

        // ================= 渲染方法 =================

        public String render() {
            try {
                Context context = new Context();
                context.setVariables(ctx);
                return resvg.render(svg, context);
            } catch (Exception e) {
                throw new RuntimeException("SvgRenderer: 渲染时出错", e);
            }
        }
    }
}
