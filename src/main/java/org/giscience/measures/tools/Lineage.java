package org.giscience.measures.tools;

import java.util.SortedMap;

public class Lineage {
    public static <I> double min(SortedMap<I, Number> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).min().orElse(Double.NaN);
    }

    public static <I> double max(SortedMap<I, Number> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).max().orElse(Double.NaN);
    }

    public static <I> double average(SortedMap<I, Number> s) {
        return s.values().stream().mapToDouble(Number::doubleValue).average().orElse(Double.NaN);
    }

    public static <I> double saturation(SortedMap<I, Number> s) {
        throw new RuntimeException("Not yet implemented");
    }
}
