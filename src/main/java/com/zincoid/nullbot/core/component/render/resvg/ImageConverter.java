package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.util.Base64Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class ImageConverter {

    private static final String RIP_SVG = "static/svg/rip.svg";
    private static final String OVERLAY_SVG = "static/svg/overlay.svg";
    private static final String PRTS_PNG = "static/image/PRTS.png";
    private static final String INVS_PRTS_PNG = "static/image/InvsPRTS.png";
    private static final String RIP_FONT = "static/font/Bernard MT Condensed.ttf";

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    /** RIP ：灰度化 + R.I.P. 文字 */
    public String RIP(String imagePath) throws Exception {
        resourceLoader.getCache(RIP_FONT);
        String svg = Files.readString(resourceLoader.getCache(RIP_SVG));
        Context ctx = new Context();
        ctx.setVariable("image", Resvg.toImgUri(imagePath, true));
        return resvg.render(svg, ctx);
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
        ctx.setVariable("image", Resvg.toImgUri(imagePath, false));
        ctx.setVariable("overlay", "data:image/png;base64,"
                + Base64Util.from(resourceLoader.getCache(overlayResource)));
        return resvg.render(svg, ctx);
    }
}
