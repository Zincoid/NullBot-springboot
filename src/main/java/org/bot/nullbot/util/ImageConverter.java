package org.bot.nullbot.util;

import org.bot.nullbot.entity.svg.SvgCanvas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter
{
    // Command 调用

    public static String rip(String userAvatarPath, String tempFontPath) throws Exception {
        ResourceLoader.getCached("static/fonts/Bernard MT Condensed.ttf", tempFontPath);
        Path tempPngPath = Files.createTempFile("rip_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640,
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
            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return base64;
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }

    public static String prts(String userAvatarPath, String tempFontPath) throws Exception {
        Path prts = ResourceLoader.getCached("static/image/prts.png", tempFontPath);
        Path tempPngPath = Files.createTempFile("prts_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640,
                    Path.of(userAvatarPath), false
            );
            // 添加 PRTS
            canvas.image(
                    0, 0, 640, 640,
                    prts, false
            );

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.render(tempPngPath, tempFontPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return base64;
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
