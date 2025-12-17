package org.bot.nullbot.util.convert;

import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.util.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter
{
    // Command 调用

    public static String rip(String userAvatarPath, String tempFontPath) throws Exception {
        ResourceLoader.getCached("static/fonts/Rubik-Bold.ttf", tempFontPath);
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
            canvas.text(150, 550, "R.I.P.")
                    .font("Rubik")
                    .size(150)
                    .color("#000000")
                    .bold()
                    .stroke("#FFFFFF", 8);

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.renderToImg(tempPngPath, tempFontPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return base64;
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
