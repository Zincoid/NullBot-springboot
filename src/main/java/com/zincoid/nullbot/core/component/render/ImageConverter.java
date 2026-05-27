package com.zincoid.nullbot.core.component.render;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import com.zincoid.nullbot.core.model.renderer.svg.SvgCanvas;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.util.Base64Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ImageConverter {

    private static final int CANVAS_WIDTH = 640;
    private static final int CANVAS_HEIGHT = 640;
    private static final String PRTS_OVERLAY = "static/image/PRTS.png";
    private static final String INVS_PRTS_OVERLAY = "static/image/InvsPRTS.png";
    private static final String RIP_FONT_RESOURCE = "static/font/Bernard MT Condensed.ttf";
    private static final String RIP_FONT_NAME = "Bernard MT Condensed";

    private final ResourceLoader resourceLoader;
    private final FileStorageProperties fileStorageProperties;

    public String RIP(String imagePath) throws Exception {
        // 确保字体文件已提取到临时目录，供 ResvgJNI LoadFontsDir 加载
        resourceLoader.getCached(RIP_FONT_RESOURCE);
        return renderCanvas("RIP_", canvas -> {
            canvas.image(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, 1, Path.of(imagePath), true);
            canvas.text(175, 550, "R.I.P.")
                    .font(RIP_FONT_NAME)
                    .size(150)
                    .color("#000000")
                    .stroke("#FFFFFF", 6);
        });
    }

    public String PRTS(String imagePath) throws Exception {
        return overlayImage(imagePath, PRTS_OVERLAY, "PRTS_");
    }

    public String invsPRTS(String imagePath) throws Exception {
        return overlayImage(imagePath, INVS_PRTS_OVERLAY, "InvsPRTS_");
    }

    private String overlayImage(String imagePath, String overlayResource, String tempPrefix) throws Exception {
        Path overlay = resourceLoader.getCached(overlayResource);
        return renderCanvas(tempPrefix, canvas -> {
            canvas.image(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, 1, Path.of(imagePath), false);
            canvas.image(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, 1, overlay, false);
        });
    }

    private String renderCanvas(String tempPrefix, Consumer<SvgCanvas> drawer) throws Exception {
        Path tempPngPath = Files.createTempFile(tempPrefix, ".png");
        try {
            SvgCanvas canvas = SvgCanvas.create(CANVAS_WIDTH, CANVAS_HEIGHT);
            drawer.accept(canvas);
            canvas.render(tempPngPath, fileStorageProperties.getTempPath());
            return Base64Util.from(tempPngPath);
        } finally {
            Files.deleteIfExists(tempPngPath);
        }
    }
}
