package org.bot.nullbot.entity.svg;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.bot.nullbot.util.render.ResvgRenderer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SvgCanvas
{
    private final Document document;
    private final Element svg;
    private final Map<String, String> fontMap = new HashMap<>();

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

    /* ---------------- 字体 ---------------- */

    public SvgCanvas font(String name, Path fontFile) {
        String css = """
            @font-face {
              font-family: '%s';
              src: url('file://%s');
            }
            """.formatted(name, fontFile.toAbsolutePath());

        Element style = document.createElement("style");
        style.setTextContent(css);
        svg.appendChild(style);

        fontMap.put(name, name);
        return this;
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
        try {
            byte[] bytes = Files.readAllBytes(imagePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            String mime;
            if (imagePath.toString().endsWith(".png")) {
                mime = "image/png";
            } else if (imagePath.toString().endsWith(".jpg")
                    || imagePath.toString().endsWith(".jpeg")) {
                mime = "image/jpeg";
            } else {
                throw new IllegalArgumentException("Unsupported image type");
            }

            Element image = document.createElement("image");
            image.setAttribute("x", String.valueOf(x));
            image.setAttribute("y", String.valueOf(y));
            image.setAttribute("width", String.valueOf(width));
            image.setAttribute("height", String.valueOf(height));

            // SVG2 标准属性
            image.setAttribute(
                    "href",
                    "data:" + mime + ";base64," + base64
            );

            svg.appendChild(image);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public Path renderToPng(Path outputPng) throws Exception {
        Path svgFile = Files.createTempFile("canvas-", ".svg");
        exportSvg(svgFile);
        ResvgRenderer.render(svgFile, outputPng);
        return outputPng;
    }
}

