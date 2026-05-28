package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class RespRenderer {

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    String cmdUses(long times) {
        // resourceLoader.getCache();
        // String svg = Files.readString(resourceLoader.getCache());
        // Context ctx = new Context();
        // ctx.setVariable();
        // return resvg.render(svg, ctx);
        return resvg.render("<svg></svg>");
    }
}
