package org.giscience.measures.tools;

import java.util.SortedMap;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class Lineage {
    public static <I, N extends Number> Number min(SortedMap<I, N> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).min().orElse(Double.NaN);
    }

    public static <I, N extends Number> Number max(SortedMap<I, N> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).max().orElse(Double.NaN);
    }

    public static <I, N extends Number> Number average(SortedMap<I, N> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).average().orElse(Double.NaN);
    }

    public static <I, N extends Number> Number saturation(SortedMap<I, N> s) {
        throw new RuntimeException("Not yet implemented");
    }
}
