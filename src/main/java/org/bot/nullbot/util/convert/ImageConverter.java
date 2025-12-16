package org.bot.nullbot.util.convert;

import org.bot.nullbot.entity.svg.SvgCanvas;
import org.bot.nullbot.util.ResourceLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter
{
    public static String rip(String userAvatarPath) throws Exception {
        Path tempPngPath = Files.createTempFile("rip_", ".png");
        Path fontPath = ResourceLoader.getCached("static/fonts/Gilroy-Bold.ttf");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(640, 640)
                    .font("target", fontPath);
            // 添加用户头像
            canvas.image(
                    0, 0, 640, 640,
                    Path.of(userAvatarPath), true
            );
            // 添加 RIP 文字
            canvas.text(200, 550, "R.I.P")
                    .font("target")
                    .size(100)
                    .color("#000000")
                    .bold()
                    .stroke("#FFFFFF", 3);

            // 使用 resvg 渲染为 PNG 并转换为 Base64
            canvas.renderToImg(tempPngPath);
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return base64;
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
