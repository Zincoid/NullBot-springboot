package org.bot.nullbot.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class StaticResourceUtil
{
    public static String loadImageAsBase64(String path) throws IOException {
        return Base64.getEncoder().encodeToString(loadImageAsStream(path));
    }

    public static byte[] loadImageAsStream(String path) throws IOException {
        Resource resource = new ClassPathResource("static/" +  path);
        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}
