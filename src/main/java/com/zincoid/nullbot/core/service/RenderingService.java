package com.zincoid.nullbot.core.service;

public interface RenderingService {

    String rip(String imagePath);

    String prts(String imagePath, boolean invert);

    String uses(long uses);

    String choyen(String topText, String bottomText);

    String pucci(String text);

    String symmetry(String imagePath, String mode);
}
