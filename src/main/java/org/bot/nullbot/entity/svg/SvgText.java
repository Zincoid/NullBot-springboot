package org.bot.nullbot.entity.svg;

import org.w3c.dom.Element;

public class SvgText {

    private final Element text;

    SvgText(Element text) {
        this.text = text;
    }

    public SvgText font(String fontName) {
        text.setAttribute("font-family", fontName);
        return this;
    }

    public SvgText size(int px) {
        text.setAttribute("font-size", px + "px");
        return this;
    }

    public SvgText color(String color) {
        text.setAttribute("fill", color);
        return this;
    }

    public SvgText bold() {
        text.setAttribute("font-weight", "bold");
        return this;
    }

    public SvgText anchorMiddle() {
        text.setAttribute("text-anchor", "middle");
        return this;
    }

    public SvgText anchorEnd() {
        text.setAttribute("text-anchor", "end");
        return this;
    }

    // 描边颜色
    public SvgText stroke(String color) {
        text.setAttribute("stroke", color);
        return this;
    }

    // 描边宽度
    public SvgText strokeWidth(int px) {
        text.setAttribute("stroke-width", px + "px");
        return this;
    }

    // 描边虚线样式
    public SvgText strokeDashArray(String dashArray) {
        text.setAttribute("stroke-dasharray", dashArray);
        return this;
    }

    // 描边线帽样式
    public SvgText strokeLinecap(String linecap) {
        text.setAttribute("stroke-linecap", linecap);
        return this;
    }

    // 描边连接样式
    public SvgText strokeLinejoin(String linejoin) {
        text.setAttribute("stroke-linejoin", linejoin);
        return this;
    }

    // 描边透明度
    public SvgText strokeOpacity(double opacity) {
        text.setAttribute("stroke-opacity", String.valueOf(opacity));
        return this;
    }

    // 组合方法：设置描边（颜色 + 宽度）
    public SvgText stroke(String color, int width) {
        return this.stroke(color).strokeWidth(width);
    }
}
