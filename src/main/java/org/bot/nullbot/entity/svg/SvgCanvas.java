package org.bot.nullbot.entity.svg;

import lombok.extern.slf4j.Slf4j;
import me.aloic.ResvgJNI;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
public class SvgCanvas
{
    private final Document document;
    private final Element svg;

    private SvgCanvas(int width, int height) {
        DOMImplementation impl =
                SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

        document = impl.createDocument(svgNS, "svg", null);
        svg = document.getDocumentElement();

        svg.setAttribute("xmlns", svgNS);
        svg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        svg.setAttribute("width", String.valueOf(width));
        svg.setAttribute("height", String.valueOf(height));
        svg.setAttribute("viewBox", "0 0 " + width + " " + height);
    }

    public static SvgCanvas create(int width, int height) {
        return new SvgCanvas(width, height);
    }

    /* ---------------- 文本 ---------------- */

    public SvgText text(int x, int y, String content) {
        Element text = document.createElement("text");
        text.setAttribute("x", String.valueOf(x));
        text.setAttribute("y", String.valueOf(y));
        text.setTextContent(content);

        svg.appendChild(text);
        return new SvgText(text);
    }

    /* ---------------- 图片 ---------------- */

    public SvgCanvas image(int x, int y, int width, int height, Path imagePath) {
        return image(x, y, width, height, imagePath, false); // 默认不转换为黑白
    }

    public SvgCanvas image(int x, int y, int width, int height, Path imagePath, boolean convertToGrayscale) {
        try {
            // 读取原始图像
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                throw new IllegalArgumentException("无法读取图像文件: " + imagePath);
            }

            byte[] imageBytes;
            String mimeType;

            if (convertToGrayscale) {
                // 转换为灰度图像
                BufferedImage grayImage = convertToGrayscale(originalImage);

                // 编码为PNG格式（PNG支持灰度）
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(grayImage, "PNG", baos);
                imageBytes = baos.toByteArray();
                mimeType = "image/png";
            } else {
                // 保持原样
                imageBytes = Files.readAllBytes(imagePath);

                // 根据文件扩展名确定MIME类型
                String fileName = imagePath.toString().toLowerCase();
                if (fileName.endsWith(".png")) {
                    mimeType = "image/png";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    mimeType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    mimeType = "image/gif";
                } else if (fileName.endsWith(".bmp")) {
                    mimeType = "image/bmp";
                } else if (fileName.endsWith(".webp")) {
                    mimeType = "image/webp";
                } else {
                    throw new IllegalArgumentException("不支持的图像格式: " + imagePath);
                }
            }

            // Base64编码
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            // 创建SVG图像元素
            Element image = document.createElement("image");
            image.setAttribute("x", String.valueOf(x));
            image.setAttribute("y", String.valueOf(y));
            image.setAttribute("width", String.valueOf(width));
            image.setAttribute("height", String.valueOf(height));

            // 使用SVG2标准的href属性
            image.setAttribute(
                    "href",
                    "data:" + mimeType + ";base64," + base64
            );

            // 如果需要，也可以添加兼容性属性
            image.setAttributeNS(
                    "http://www.w3.org/1999/xlink",
                    "xlink:href",
                    "data:" + mimeType + ";base64," + base64
            );

            svg.appendChild(image);
            return this;

        } catch (Exception e) {
            throw new RuntimeException("添加图像失败: " + imagePath, e);
        }
    }

    /* ---------------- 灰度工具 ---------------- */

    private BufferedImage convertToGrayscale(BufferedImage original) {
        // 创建灰度图像
        BufferedImage grayImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        // 绘制并自动转换为灰度
        Graphics2D g2d = grayImage.createGraphics();

        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制图像（会自动转换为灰度）
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return grayImage;
    }

    /* ---------------- 背景颜色 ---------------- */

    public SvgCanvas backgroundColor(String color) {
        Element rect = document.createElement("rect");
        rect.setAttribute("x", "0");
        rect.setAttribute("y", "0");
        rect.setAttribute("width", "100%");
        rect.setAttribute("height", "100%");
        rect.setAttribute("fill", color);

        svg.insertBefore(rect, svg.getFirstChild());
        return this;
    }

    /* ---------------- 导出 ---------------- */

    public Path exportSvg(Path path) throws Exception {
        Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(
                new DOMSource(document),
                new StreamResult(path.toFile())
        );
        return path;
    }

    /* ---------------- 渲染 ---------------- */

    public Path render(Path output, String fontDir) throws Exception {
        Path svgFile = Files.createTempFile("canvas-", ".svg");
        exportSvg(svgFile);

        // 工作目录 建议改成自定义 临时目录
        var options = new ResvgJNI.RenderOptions("/tmp");
        // 字体目录
        options.LoadFontsDir(fontDir);

        var renderer = new ResvgJNI.Renderer(options);
        var inputFilePath = svgFile.toAbsolutePath().toString();
        var outputFilePath = output.toAbsolutePath().toString();
        var svgData = Files.readString(Path.of(inputFilePath));

        try {
            var data = renderer.RenderPng(svgData);
            Files.write(Path.of(outputFilePath), data);
        } catch (Exception e) {
            log.error("Resvg JNI 出错: {}", e.getMessage());
        } finally {
            Files.deleteIfExists(svgFile);
        }

        return output;
    }
}

