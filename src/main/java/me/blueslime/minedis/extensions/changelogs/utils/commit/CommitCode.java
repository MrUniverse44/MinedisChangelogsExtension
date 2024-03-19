package me.blueslime.minedis.extensions.changelogs.utils.commit;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CommitCode {
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";

    public static String generate() {
        Random random = ThreadLocalRandom.current();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            int indice = random.nextInt(CHARACTERS.length());

            result.append(CHARACTERS.charAt(indice));
        }
        return result.toString();
    }
}
