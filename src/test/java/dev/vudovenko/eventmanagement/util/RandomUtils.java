package dev.vudovenko.eventmanagement.util;

import java.security.SecureRandom;

public class RandomUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RandomUtils() {
    }

    public static int getRandomInt() {
        return SECURE_RANDOM.nextInt();
    }
}
