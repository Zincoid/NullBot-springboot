package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class RespRenderer {

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        resourceLoader.getCache("static/font/MonomaniacOne-Regular.ttf");
    }

    public String cmdUses(long uses) throws IOException {
        Path svgPath = resourceLoader.getCache("static/svg/uses.svg");
        String svg = Files.readString(svgPath);
        Context ctx = new Context();
        ctx.setVariable("uses", uses);
        return resvg.render(svg, ctx);
    }
}
