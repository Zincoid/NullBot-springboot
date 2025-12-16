package org.bot.nullbot.util.image;

import org.bot.nullbot.entity.svg.SvgCanvas;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageConverter
{
    public static String rip(String userAvatarPath) throws Exception {
        Path tempPngPath = Files.createTempFile("rip_", ".png");
        try {
            // 创建 SVG 画布
            SvgCanvas canvas = SvgCanvas.create(800, 600)
                    .font("target", Path.of("src/main/resources/static/fonts/Gilroy-Bold.ttf"));
            // 添加 RIP 文字
            canvas.text(400, 150, "R.I.P")
                    .font("target")
                    .size(18)
                    .color("#000000")
                    .anchorMiddle();
            // 添加用户头像
            canvas.image(
                    300, 200, 200, 200,
                    Path.of(userAvatarPath), true
            );

            // 使用 resvg 渲染为 PNG
            canvas.renderToImg(tempPngPath);
            // 读取 PNG 文件并转换为 Base64
            byte[] pngBytes = Files.readAllBytes(tempPngPath);
            String base64 = Base64.getEncoder().encodeToString(pngBytes);
            return base64;
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
