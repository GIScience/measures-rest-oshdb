package org.giscience.measures.tools;

import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class Index {
    public static <I, R, S> TreeMap<I, R> map(SortedMap<I, S> m, Function<S, R> f) {
        return m.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> f.apply(e.getValue()), (v1, v2) -> {
            throw new RuntimeException("Duplicate keys never occur.");
        }, TreeMap::new));
    }

    public static <I, R, S> TreeMap<I, R> mapStream(SortedMap<I, Collection<S>> m, Function<Stream<S>, R> f) {
        return Index.map(m, c -> f.apply(c.stream()));
    }

    private static <I, J, R> TreeMap<I, SortedMap<J, R>> regroupCombinedIndex(SortedMap<OSHDBCombinedIndex<I, J>, R> data) {
        return data.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getFirstIndex(), e -> {
            SortedMap<J, R> m = new TreeMap<>();
            m.put(e.getKey().getSecondIndex(), e.getValue());
            return m;
        }, (m1, m2) -> {
            m1.putAll(m2);
            return m1;
        }, TreeMap::new));
    }

    public static <I, J, R, S> TreeMap<I, R> reduce(SortedMap<OSHDBCombinedIndex<I, J>, S> m, Function<SortedMap<J, S>, R> f) {
        return Index.regroupCombinedIndex(m).entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> f.apply(e.getValue()), (v1, v2) -> {
            throw new RuntimeException("Duplicate keys never occur.");
        }, TreeMap::new));
    }
}
