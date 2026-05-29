package com.zincoid.nullbot.core.service.impl;

import com.zincoid.nullbot.core.component.render.browser.HtmlRenderer;
import com.zincoid.nullbot.core.component.render.resvg.SvgRenderer;
import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.service.RenderingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RenderingServiceImpl implements RenderingService {

    private final SvgRenderer svgRenderer;
    private final HtmlRenderer htmlRenderer;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        resourceLoader.getCache("static/font/Bernard-MT-Condensed.ttf");
        resourceLoader.getCache("static/font/MonomaniacOne-Regular.ttf");
    }

    @Override
    public String rip(String imagePath) {
        return svgRenderer.load("rip")
                .image("image", imagePath, true)
                .render();
    }

    @Override
    public String prts(String imagePath, boolean invert) {
        String prtsResourcePath = invert ? "static/image/InvsPRTS.png" : "static/image/PRTS.png";
        return svgRenderer.load("prts")
                .image("image", imagePath, false)
                .resource("prts", prtsResourcePath, false)
                .render();
    }

    @Override
    public String uses(long uses) {
        return svgRenderer.load("uses")
                .number("uses", uses)
                .render();
    }

    @Override
    public String choyen(String topText, String bottomText) {
        return htmlRenderer.load("static/html/5000choyen.html")
                .string("topText", topText)
                .string("bottomText", bottomText)
                .render("#templateContainer");
    }

    @Override
    public String pucci(String text) {
        return htmlRenderer.load("static/html/pucci.html")
                .string("text1", "普奇！！回答我！")
                .string("text2", "为什么你要加速时间！！")
                .string("text3", text)
                .resource("background", "static/image/pucci.png")
                .render("#wrap");
    }

    @Override
    public String symmetry(String imagePath, String mode) {
        return htmlRenderer.load("static/html/symmetry.html")
                .image("image", imagePath)
                .string("mode", mode)
                .render("#mirrorContainer");
    }
}
