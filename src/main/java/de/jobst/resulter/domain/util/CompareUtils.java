package de.jobst.resulter.domain.util;

public class CompareUtils {
    // Hilfsmethode zum Vergleichen von potenziell null Werten
    public static <T extends Comparable<T>> int compareNullable(T a, T b) {
        if (a == null ^ b == null) {
            return a == null ? -1 : 1; // Null ist kleiner als Nicht-Null
        }
        if (a == null && b == null) {
            return 0; // Beide Null, also gleich
        }
        return a.compareTo(b); // Keines der beiden ist Null, normaler Vergleich
    }
}
