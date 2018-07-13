# Measures REST OSHDB

The library `Measures REST OSHDB` provides an extension to the library [Measures REST](https://github.com/giscience/measures-rest).  It aids with implementing a measure that consumes data from the [OpenStreetMap History Database (OSHDB)](???).

## Scientific Publications

The following publication is related to this framework and the used DGGS:

* F-B Mocnik: **Linked Open Data Vocabularies for Semantically Annotated Repositories of Data Quality Measures** Proceedings of the 10th International Conference on Geographic Information Science (GIScience), 2018

* F-B Mocnik, A Mobasheri, L Griesbaum, M Eckle, C Jacobs, and C Klonner: [**A grounding-based ontology of data quality measures**](http://josis.org/index.php/josis/article/viewFile/360/197) Journal of Spatial Information Science, 16, 2018

* F-B Mocnik: [**A Novel Identifier Scheme for the ISEA Aperture 3 Hexagon Discrete Global Grid System.**](http://doi.org/10.1080/15230406.2018.1455157) Cartography and Geographic Information science, 2018

* F-B Mocnik, A Zipf, and M Raifer: [**The OpenStreetMap folksonomy and its evolution.**](http://doi.org/10.1080/10095020.2017.1368193) Geo-spatial Information Science, 20(3), 2017, 219–230

## Implementing a Measure

A measure that consumes data from the [OSHDB](???) extends the class `MeasureOSHDB<R, O extends OSHDBMapReducible>`.  Here, `R` is a generic parameter that refers to the result of the measure; and `O` is the class to be mapped.  As an example, one may extend the class `MeasureOSHDB` as follows:

```java
public class MeasureLengthOfElements extends MeasureOSHDB<Number, OSMEntitySnapshot> {
    @Override
    public SortedMap<GridCell, Number> compute(MapAggregator<GridCell, OSMEntitySnapshot> mapReducer, OSHDBRequestParameter p) throws Exception {
        return mapReducer
                .osmTag("highway", "residential")
                .osmTag("maxspeed")
                .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                .sum();
    }
}
```

Instead of the function `compute(BoundingBox bbox)`, the function `compute(MapAggregator<GridCell, O> mapReducer, OSHDBRequestParameter p)` can be overwritten in order to implement the actual measure.  As a parameter, a mapReducer object is provided that already refers to the corresponding bounding box and the corresponding point in time or time span.  The mapReducer can be used to filter and aggregate the data, as is described in the documentation of the [OSHDB](???).  Whether the measure refers to one point in time or a time span is determined by the method `refersToTimeSpan` (see below).

### Using REST URL parameters

Parameters from the REST URL can easily be used in the same way as in [Measure REST](https://github.com/giscience/measures-rest).  As an example, `p.get("hello").toString()` provides the value for the key `hello` provided in the URL.

To simplify the filtering by a tag (key and value) in the OSHDB, one can filter like follows:

```java
mapReducer.osmTag(p.getOSMTag());
```

In this case, the `key` and `value` provided in the URL will be used for filtering.  If only a `key` is available, it is only filtered for a key.  In some cases, one might want to provide the parameters using other keys in the URL.  In this case, the keys can manually be provided, e.g., as follows:

```java
mapReducer.osmTag(p.getOSMTag("newKey", "newValue"));
```

### Default values

The default values for the measure can be defined in the class `MeasureOSDHB`.  Further information can be found in the documentation of the library [Measures REST](https://github.com/giscience/measures-rest).

```java
@Override
public Boolean refersToTimeSpan() {
    return false;
}

@Override
public ZonedDateTime defaultDate() {
    return ZonedDateTime.now(UTC).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS);
}

@Override
public ZonedDateTime defaultDateFrom() {
    return null;
}

@Override
public Integer defaultDaysBefore() {
    return 3 * 12 * 30;
}

@Override
public Integer defaultIntervalInDays() {
    return 30;
}
```

### Computing using the MapReducer result

In some cases, the data computed using the `mapReducer` need to be used in further computations.  In order to do so, the function `Index.map` can be used, for example, as follows:

```java
return Index.map(
        mapReducer.count(),
        s -> 2 * s.doubleValue()
);
```

Here, the function given in the second argument only refers to the values of the `SortedMap`, while the keys (in our case the grid cells) stay unchanged.

### Lineage

If a measure refers to a time span, the data are examined at different points in time.  These points in time are all in the time span provided by the URL or the default values given within the implementation (see below).  The interval – the number of days between two such points in time – can be defined in the implementation of the measure by overriding the method `intervalInDays`.

By default, the interval is 30 days.  The points in time used for the measures are computed as follows: the last timestamp is the date determined by the parameter `date`; the second last one, 30 days before; the third last one, 60 days before; etc.  The first date is always larger than the date determined by the parameters `dateFrom` and `daysBefore`.  In case of other intervals, the points in time are computed accordingly.

When the data have been aggregated manually by the timestamps, two indices are used: one for the grid cells and one for the timestamps.  Technically, this results in a `OSHDBCombinedIndex`.  To hide the first index by the grid cells, there is offered a function `Index.reduce` that consumes two arguments.  The first argument refers to the result of the `mapReducer` after having aggregated by timestamps, and the second argument refers to how the list of values for the different timestamps shall be used for computing the final result.  Thereby, the second argument is used for each grid cell separately.  A measure applying the saturation principle to highways looks, for example, as follows:

```java
@Override
public SortedMap<GridCell, Number> compute(MapAggregator<GridCell, OSMEntitySnapshot> mapReducer, OSHDBRequestParameter p) throws Exception {
    return Index.reduce(
            mapReducer
                    .osmTag("highway")
                    .aggregateByTimestamp()
                    .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                    .sum(),
            Lineage::saturation
    );
}
```

The function `Lineage::saturation` already provides the needed computation needed to conclude the saturation from a list of road network lengths at different points in time.  In this example, the function consumes a value of type `SortedMap<OSHDBTimestamp, Number>` and results in a `Number`.  Such functions can be inserted manually but different nearby choices already exist:

| Function | Input | Output| Description |
| ------ | ---- | ------- | ----------- |
| `Lineage::min` | `SortedMap<I, Number>` | `Number` | minimum for all timestamps |
| `Lineage::max` | `SortedMap<I, Number>` | `Number` | maximum for all timestamps |
| `Lineage::sum` | `SortedMap<I, Number>` | `Number` | sum for all timestamps |
| `Lineage::average` | `SortedMap<I, Number>` | `Number` | average for all timestamps |
| `Lineage::saturation` | `SortedMap<I, Number>` | `Number` | saturation principle |

### Casting the result

The result of a measure is a `SortedMap` with grid cells as keys and numbers as values, that is, it is of type `SortedMap<GridCell, Number>`.  Many commands like `mapReducer.sum()` return exactly this type, while others like `mapReducer.count()` return the result as `SortedMap<GridCell, Integer>`.  While `Integer` is a subclass of `Number` and can thus be casted, this does not automatically work for the `SortedMap`.  Instead, the value has to be casted manually:

```java
return Cast.result(mapReducer.count())
```

This works for all `SortedMap<GridCell, R>` with `X` being castable to `Number`.  When using `Index.computeCombinedWithAggregate` for aggregation, there should be no need to use `Cast.result` because this is already handled by `computeCombinedWithAggregate` and the corresponding function in the second argument.

### Aggregation by grid cells

The data is automatically aggregated by the `MapAggregator` into grid cells (ISEA 3H DGGS).  If, however, the data shall be aggregated manually, the method `gridCell` offers a simple way to aggregate.  It accepts either a `OSMEntitySnapshot`, a `OSMContribution`, or a geometry.  A geometry needs to be provided if different ways of aggregation are of interest, for example, when the data should not be aggregated by the centroid of the geometry but rather by the first node of the geometry, by the centroid of the convex hull, etc.

## Instantiation of the Measure

In contrast to the class `Measure`, the constructor of the class `MeasureOSHDB` accepts an `OSHDB` object as a parameter.  A typical way of running the REST server is as follows:

```java
OSHDBDatabase oshdb = new OSHDBH2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements().setOSHDB(oshdb));
restServer.run();
```

Instead of using only one database for the data as well as for the keytables, also two separate databases can be used (compare the documentation of the [OSHDB](???)):

```java
OSHDBDatabase oshdb = new OSHDBH2(...).multithreading(true);
OSHDBJdbc oshdbKeydb = new OSHDBH2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements().setOSHDB(oshdb, oshdbKeydb));
```

## Author

This software is written and maintained by Franz-Benjamin Mocnik, <mocnik@uni-heidelberg.de>, GIScience Research Group, Institute of Geography, Heidelberg University.

The development has been supported by the DFG project *A framework for measuring the fitness for purpose of OpenStreetMap data based on intrinsic quality indicators* (FA 1189/3-1).

(c) by Heidelberg University, 2017–2018.

## License

The code is licensed under the [MIT license](https://github.com/giscience/measures-rest-oshdb/blob/master/LICENSE).
