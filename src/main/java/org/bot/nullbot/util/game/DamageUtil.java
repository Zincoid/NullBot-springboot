package org.bot.nullbot.util.game;

import java.util.Random;

public class DamageUtil {

    private static final Random R = new Random();

    public static int playerDamage() {
        return 15 + R.nextInt(10);
    }

    public static int aiDamage() {
        return 10 + R.nextInt(5);
    }
}
