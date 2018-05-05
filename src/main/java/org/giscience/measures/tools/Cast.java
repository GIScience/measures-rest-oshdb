package org.giscience.measures.tools;

import org.giscience.utils.geogrid.cells.GridCell;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public class Cast {
    public static <N extends Number> SortedMap<GridCell, Number> result(SortedMap<GridCell, N> m) {
        return m.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> (Number) e.getValue(), (v1, v2) -> {
            throw new RuntimeException("Duplicate keys never occur.");
        }, TreeMap::new));
    }
}
