package de.jobst.resulter.domain.util;

import org.jspecify.annotations.Nullable;

public class StringUtils {

    public static String formatAsName(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean newWord = true;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (Character.isWhitespace(ch)) {
                // Append single space only if last char wasn't a space or joiner
                if (!result.isEmpty() && result.charAt(result.length() - 1) != ' ' && !isJoiner(result.charAt(result.length() - 1))) {
                    result.append(' ');
                }
                newWord = true;
            } else if (isJoiner(ch)) {
                // Remove trailing space if present
                if (!result.isEmpty() && result.charAt(result.length() - 1) == ' ') {
                    result.setLength(result.length() - 1);
                }
                result.append(ch);
                newWord = true;

                // 🔥 SKIP whitespace after the joiner
                while (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    i++;
                }
            } else {
                result.append(newWord ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
                newWord = false;
            }
        }

        return result.toString().trim();
    }

    private static boolean isJoiner(char ch) {
        return ch == '-' || ch == '/' || ch == '+' || ch == '\'';
    }

}
