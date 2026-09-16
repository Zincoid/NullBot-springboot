package com.zincoid.nullbot.core.module.render.resvg;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

@Disabled("须渲染环境, 手动验证")
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SvgRendererTests {

    @Resource
    private SvgRenderer svgRenderer;

    @Test
    void renderUses() throws IOException {
        String file = "src/test/file/uses.png";
        String base64 = svgRenderer.load("uses").number("uses", 123456).render();
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }
}
