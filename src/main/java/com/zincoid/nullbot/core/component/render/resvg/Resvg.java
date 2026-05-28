package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.util.Base64Util;
import lombok.RequiredArgsConstructor;
import me.aloic.ResvgJNI;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class Resvg {

    private static final TemplateEngine TEMPLATE_ENGINE;

    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(resolver);
    }

    private final FileStorageProperties fileStorageProperties;

    /** SVG + CTX → PNG */
    public String render(String svg, Context ctx) {
        return render(TEMPLATE_ENGINE.process(svg, ctx));
    }

    /** SVG → PNG */
    public String render(String svg) {
        String dir = fileStorageProperties.getTempPath();
        var opts = new ResvgJNI.RenderOptions(dir);
        opts.LoadFontsDir(dir);
        return Base64.getEncoder().encodeToString(new ResvgJNI.Renderer(opts).RenderPng(svg));
    }

    /** IMG → Data URI */
    public static String toImgUri(String path, boolean grayscale) throws Exception {
        BufferedImage img = ImageIO.read(Path.of(path).toFile());
        if (img == null) throw new IllegalArgumentException("无法读取图像: " + path);
        if (grayscale) {
            BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, null);
            g.dispose();
            img = gray;
        }
        return "data:image/png;base64," + Base64Util.from(img);
    }
}
