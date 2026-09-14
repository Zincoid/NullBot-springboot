package com.zincoid.nullbot.core.module.render.resvg;

import com.zincoid.nullbot.core.properties.file.StorageProperties;
import lombok.RequiredArgsConstructor;
import me.aloic.ResvgJNI;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class Resvg {

    private static final TemplateEngine TEMPLATE_ENGINE;

    static {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("static/svg/");
        resolver.setSuffix(".svg");
        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(resolver);
    }

    private final StorageProperties storageProperties;

    /** SVG + CTX → PNG */
    public String render(String svg, Context ctx) {
        return render(TEMPLATE_ENGINE.process(svg, ctx));
    }

    /** SVG → PNG */
    public String render(String svg) {
        String tempPath = storageProperties.getTempPath();
        String workDir = storageProperties.resolve(tempPath);
        var opts = new ResvgJNI.RenderOptions(workDir);
        opts.LoadFontsDir(workDir);
        return Base64.getEncoder().encodeToString(new ResvgJNI.Renderer(opts).RenderPng(svg));
    }
}
