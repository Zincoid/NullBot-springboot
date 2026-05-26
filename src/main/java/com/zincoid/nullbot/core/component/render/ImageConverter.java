package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.model.renderer.svg.SvgCanvas;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.util.Base64Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class ImageConverter {

    private final ResourceLoader resourceLoader;
    private final FileStorageProperties fileStorageProperties;

    public String RIP(String imagePath) throws Exception {
        resourceLoader.getCached("static/font/Bernard MT Condensed.ttf");
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
            canvas.render(tempPngPath, fileStorageProperties.getTempPath());
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String PRTS(String imagePath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/PRTS.png");
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
            canvas.render(tempPngPath, fileStorageProperties.getTempPath());
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String invsPRTS(String imagePath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/InvsPRTS.png");
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
            canvas.render(tempPngPath, fileStorageProperties.getTempPath());
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
