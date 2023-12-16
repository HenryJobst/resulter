package de.jobst.resulter.domain.util;

public class StringUtils {

    public static String formatAsName(String text) {
        StringBuilder result = new StringBuilder();
        boolean newWord = true; // Markiert den Beginn eines neuen Wortes

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean isSeparator = (ch == ' ' || ch == '-' || ch == '/' || ch == '+');

            if (isSeparator) {
                if (newWord && !result.isEmpty() && result.charAt(result.length() - 1) == ' ') {
                    result.setLength(result.length() - 1); // Entferne das zusätzliche Leerzeichen vor dem Separator
                }
                result.append(ch);
                newWord = true;
            } else {
                if (newWord) {
                    if (i > 0 && text.charAt(i - 1) == ' ' &&
                            !result.isEmpty() && result.charAt(result.length() - 1) == ' ') {
                        result.setLength(result.length() - 1); // Entferne das zusätzliche Leerzeichen vor dem Wort
                    }
                    result.append(Character.toUpperCase(ch)); // Erster Buchstabe groß
                    newWord = false;
                } else {
                    result.append(Character.toLowerCase(ch)); // Folgebuchstaben klein
                }
            }
        }

        return result.toString().trim();
    }
}
