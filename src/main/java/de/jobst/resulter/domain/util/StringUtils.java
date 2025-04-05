package de.jobst.resulter.domain.util;

public class StringUtils {

    public static String formatAsName(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean newWord = true;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean isSeparator = isSeparator(ch);

            if (isSeparator) {
                if (!result.isEmpty() && !isSeparator(result.charAt(result.length() - 1))) {
                    result.append(ch);
                }
                newWord = true;
            } else {
                result.append(newWord ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
                newWord = false;
            }
        }

        return result.toString().trim();
    }

    private static boolean isSeparator(char ch) {
        return ch == ' ' || ch == '-' || ch == '/' || ch == '+';
    }

    private static boolean isSpace(char ch) {
        return ch == ' ';
    }
}
