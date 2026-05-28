package com.zincoid.nullbot.core.component.render.resvg;

import com.zincoid.nullbot.core.component.resource.ResourceLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class RespRenderer {

    private final Resvg resvg;
    private final ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        resourceLoader.getCache("static/font/MonomaniacOne-Regular.ttf");
    }

    public String cmdUses(long uses) {
        Context ctx = new Context();
        ctx.setVariable("uses", uses);
        return resvg.render("uses", ctx);
    }
}
