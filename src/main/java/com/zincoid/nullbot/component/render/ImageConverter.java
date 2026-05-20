package com.zincoid.nullbot.component.render;

import com.zincoid.nullbot.component.resource.ResourceLoader;
import com.zincoid.nullbot.config.prop.FileStorageProperties;
import com.zincoid.nullbot.entity.svg.SvgCanvas;
import com.zincoid.nullbot.util.Base64Util;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ImageConverter {

    private final ResourceLoader resourceLoader;

    private final String tempFontPath;
    private final String tempImagePath;

    public ImageConverter(ResourceLoader resourceLoader, FileStorageProperties fileStorageProperties) {
        this.resourceLoader = resourceLoader;
        tempFontPath = fileStorageProperties.getTempPath() + "/font";
        tempImagePath = fileStorageProperties.getTempPath() + "/image";
    }

    public String RIP(String imagePath) throws Exception {
        resourceLoader.getCached("static/font/Bernard MT Condensed.ttf", tempFontPath);
        Path tempPngPath = Files.createTempFile("RIP_", ".png");
        try {
            // 创建 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加 图片
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(imagePath), true
            );
            // 添加 RIP
            canvas.text(175, 550, "R.I.P.")
                    .font("Bernard MT Condensed")
                    .size(150)
                    .color("#000000")
                    // .bold()
                    .stroke("#FFFFFF", 6);
            // 渲染并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String PRTS(String imagePath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/PRTS.png", tempImagePath);
        Path tempPngPath = Files.createTempFile("PRTS_", ".png");
        try {
            // 创建 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加 图片
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(imagePath), false
            );
            // 添加 PRTS
            canvas.image(
                    0, 0, 640, 640, 1,
                    prts, false
            );
            // 渲染并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String invsPRTS(String imagePath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/InvsPRTS.png", tempImagePath);
        Path tempPngPath = Files.createTempFile("InvsPRTS_", ".png");
        try {
            // 创建 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加 图片
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(imagePath), false
            );
            // 添加 PRTS
            canvas.image(
                    0, 0, 640, 640, 1,
                    prts, false
            );
            // 渲染并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
