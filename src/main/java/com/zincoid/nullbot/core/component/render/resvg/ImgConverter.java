package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.util.Base64Util;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class ImgConverter {

    private static final String PRTS_PNG = "static/image/PRTS.png";
    private static final String INVS_PRTS_PNG = "static/image/InvsPRTS.png";

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        resourceLoader.getCache("static/font/Bernard-MT-Condensed.ttf");
    }

    /** RIP ：灰度化 + R.I.P. 文字 */
    public String RIP(String imagePath) {
        Context ctx = new Context();
        ctx.setVariable("image", Resvg.toImgUri(imagePath, true));
        return resvg.render("rip", ctx);
    }

    /** PRTS ：封锁效果 */
    public String PRTS(String imagePath) {
        return overlay(imagePath, PRTS_PNG);
    }

    /** PRTS ：封锁反色效果 */
    public String invsPRTS(String imagePath) {
        return overlay(imagePath, INVS_PRTS_PNG);
    }

    /** Overlay ：叠加效果 */
    private String overlay(String imagePath, String overlayPath) {
        Context ctx = new Context();
        ctx.setVariable("image", Resvg.toImgUri(imagePath, false));
        ctx.setVariable("overlay", "data:image/png;base64,"
                + Base64Util.from(resourceLoader.getCache(overlayPath)));
        return resvg.render("overlay", ctx);
    }
}
