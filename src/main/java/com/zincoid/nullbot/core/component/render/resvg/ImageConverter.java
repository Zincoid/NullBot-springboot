package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ImageConverter {

    private static final String RIP_SVG = "static/svg/rip.svg";
    private static final String OVERLAY_SVG = "static/svg/overlay.svg";
    private static final String PRTS_PNG = "static/image/PRTS.png";
    private static final String INVS_PRTS_PNG = "static/image/InvsPRTS.png";
    private static final String RIP_FONT = "static/font/Bernard MT Condensed.ttf";

    private static final TemplateEngine TEMPLATE_ENGINE;

    static {
        StringTemplateResolver resolver = new StringTemplateResolver();
        TEMPLATE_ENGINE = new TemplateEngine();
        TEMPLATE_ENGINE.setTemplateResolver(resolver);
    }

    private final ResourceLoader resourceLoader;
    private final FileStorageProperties fileStorageProperties;

    // =========================== 图像方法 ===========================

    /** RIP ：灰度化 + R.I.P. 文字 */
    public String RIP(String imagePath) throws Exception {
        resourceLoader.getCache(RIP_FONT);
        String svg = Files.readString(resourceLoader.getCache(RIP_SVG));
        Context ctx = new Context();
        ctx.setVariable("image", imageDataUri(imagePath, true));
        return render(TEMPLATE_ENGINE.process(svg, ctx));
    }

    /** PRTS ：封锁效果 */
    public String PRTS(String imagePath) throws Exception {
        return overlay(imagePath, PRTS_PNG);
    }

    /** PRTS ：封锁反色效果 */
    public String invsPRTS(String imagePath) throws Exception {
        return overlay(imagePath, INVS_PRTS_PNG);
    }

    /** Overlay ：叠加效果 */
    private String overlay(String imagePath, String overlayResource) throws Exception {
        String svg = Files.readString(resourceLoader.getCache(OVERLAY_SVG));
        Context ctx = new Context();
        ctx.setVariable("image", imageDataUri(imagePath, false));
        ctx.setVariable("overlay", "data:image/png;base64,"
                + Base64Util.from(resourceLoader.getCache(overlayResource)));
        return render(TEMPLATE_ENGINE.process(svg, ctx));
    }

    // =========================== 工具方法 ===========================

    /** SVG → PNG */
    private String render(String svg) {
        String dir = fileStorageProperties.getTempPath();
        var opts = new ResvgJNI.RenderOptions(dir);
        opts.LoadFontsDir(dir);
        return Base64.getEncoder().encodeToString(new ResvgJNI.Renderer(opts).RenderPng(svg));
    }

    /** 图片 → data URI，可选灰度转换 */
    private String imageDataUri(String path, boolean grayscale) throws Exception {
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
