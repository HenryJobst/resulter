package de.jobst.resulter.domain.util;

public class StringUtils {

    public static String formatAsName(String text) {
        StringBuilder result = new StringBuilder();
        boolean newWord = true; // Markiert den Beginn eines neuen Wortes

        for (char ch : text.toCharArray()) {
            if (ch == ' ' || ch == '-' || ch == '/') {
                newWord = true; // Nächstes Zeichen wird Anfang eines neuen Wortes sein
                result.append(ch);
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
}
