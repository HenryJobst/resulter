package de.jobst.resulter.domain.util;

public class StringUtils {

    public static String formatAsName(String text) {
        StringBuilder result = new StringBuilder();
        boolean newWord = true; // Markiert den Beginn eines neuen Wortes

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean isSeparator = isSeparator(ch);

            if (isSeparator) {
                int lastCharIndex = result.length() - 1;
                char lastChar = result.charAt(lastCharIndex);
                if (newWord && !result.isEmpty() && isSpace(lastChar)) {
                    result.setLength(lastCharIndex); // Entferne das zusätzliche Leerzeichen vor dem Separator
                    result.append(ch);
                } else if (newWord && !result.isEmpty() && isSeparator(lastChar)) {
                    // ignoriere aktuellen Separator
                } else {
                    result.append(ch);
                }
                newWord = true;
            } else {
                if (newWord) {
                    result.append(Character.toUpperCase(ch)); // Erster Buchstabe groß
                    newWord = false;
                } else {
                    result.append(Character.toLowerCase(ch)); // Folgebuchstaben klein
                }
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
