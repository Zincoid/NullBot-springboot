package com.zincoid.nullbot.core.service.render;

public interface RenderingService {

    String rip(String imagePath);

    String prts(String imagePath, boolean invert);

    String usage(String avatarPath, long times);

    String choyen(String topText, String bottomText);

    String pucci(String text);

    String symmetry(String imagePath, String mode);
}
