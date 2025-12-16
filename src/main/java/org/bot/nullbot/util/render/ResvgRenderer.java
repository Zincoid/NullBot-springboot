package org.bot.nullbot.util.render;

import java.nio.file.Path;

public class ResvgRenderer
{
    private ResvgRenderer() {}

    public static void render(Path svg, Path png) throws Exception {
        Process process = new ProcessBuilder(
                "resvg",
                svg.toAbsolutePath().toString(),
                png.toAbsolutePath().toString()
        ).inheritIO().start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("resvg render failed, code=" + exitCode);
        }
    }
}