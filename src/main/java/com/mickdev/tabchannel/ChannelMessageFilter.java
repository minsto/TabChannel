package com.mickdev.tabchannel;

import java.util.Locale;
import java.util.Set;

public final class ChannelMessageFilter {

    private static final Set<String> BAD_WORDS = Set.of(
            "merde", "putain", "fdp", "encule", "salope",
            "fuck", "shit", "bitch"
    );

    private ChannelMessageFilter() {
    }

    public static boolean containsBlockedWords(String input) {
        String norm = normalize(input);
        for (String bad : BAD_WORDS) {
            if (norm.contains(normalize(bad))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String in) {
        return in.toLowerCase(Locale.ROOT)
                .replace("0", "o")
                .replace("1", "i")
                .replace("3", "e")
                .replace("4", "a")
                .replace("5", "s")
                .replace("@", "a")
                .replace("$", "s")
                .replaceAll("[^a-zA-Zàâçéèêëîïôûùüÿñæœ]", "");
    }
}