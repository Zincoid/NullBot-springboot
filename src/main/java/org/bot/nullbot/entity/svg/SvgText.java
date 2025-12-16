package org.bot.nullbot.entity.svg;

import org.w3c.dom.Element;

public class SvgText
{
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
}
