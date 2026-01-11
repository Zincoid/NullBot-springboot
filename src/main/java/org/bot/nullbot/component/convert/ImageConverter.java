package org.bot.nullbot.component.convert;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.resource.ResourceLoader;
import org.bot.nullbot.entity.svg.SvgCanvas;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ImageConverter
{
    private final ResourceLoader resourceLoader;

    public String RIP(String userAvatarPath, String tempFontPath) throws Exception {
        resourceLoader.getCached("static/fonts/Bernard MT Condensed.ttf", tempFontPath);
        Path tempPngPath = Files.createTempFile("RIP_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(userAvatarPath), true
            );
            // 添加 RIP 文字
            canvas.text(175, 550, "R.I.P.")
                    .font("Bernard MT Condensed")
                    .size(150)
                    .color("#000000")
                    // .bold()
                    .stroke("#FFFFFF", 6);

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            return Base64.getEncoder().encodeToString(pngBytes);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String PRTS(String userAvatarPath, String tempFontPath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/PRTS.png", tempFontPath);
        Path tempPngPath = Files.createTempFile("PRTS_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(userAvatarPath), false
            );
            // 添加 PRTS
            canvas.image(
                    0, 0, 640, 640, 1,
                    prts, false
            );

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            return Base64.getEncoder().encodeToString(pngBytes);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public String inversePRTS(String userAvatarPath, String tempFontPath) throws Exception {
        Path prts = resourceLoader.getCached("static/image/InversePRTS.png", tempFontPath);
        Path tempPngPath = Files.createTempFile("InversePRTS_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640, 1,
                    Path.of(userAvatarPath), false
            );
            // 添加 PRTS
            canvas.image(
                    0, 0, 640, 640, 1,
                    prts, false
            );

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            return Base64.getEncoder().encodeToString(pngBytes);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
