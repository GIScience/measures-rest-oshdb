# Measures REST OSHDB

The library `Measues REST OSHDB` provides an extension to the library [Measures REST](https://github.com/giscience/measures-rest).  It aids with implementing a measure that consumes data from the [HeiGIT OSHDB](???).

## Implementing a Measure

A measure that consumes data from the [HeiGIT OSHDB](???) should extend the class `MeasureOSHDB<R, O extends OSHDB_MapReducible>`.  Here, `R` is a generic parameter that refers to the result of the measure; and `O` is the class to be mapped. As an example, one may extend the class `MeasureOSHDB` as follows:

```java
@Path("api/" + MeasureLengthOfElements.name)
public class MeasureLengthOfElements extends MeasureOSHDB<Double, OSMEntitySnapshot> {
    public static final String name = "measure-length-of-elements";

    public MeasureTest(OSHDB_JDBC oshdb) {
        super(oshdb);
    }

    public MeasureTest(OSHDB_Database, OSHDB_JDBC oshdb_keydb) {
        super(oshdb, oshdb_keydb);
    }

    @Override
    public SortedMap<GridCell, Number> compute(MapReducer<OSMEntitySnapshot> mapper) throws Exception {
        return mapper
                .where("highway", "residential")
                .where("maxspeed")
                .aggregateBy(this::gridCell)
                .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                .sum();
    }
}
```

Instead of the function `compute(BoundingBox bbox)`, a new function `compute(Mapper<O> mapper)` can be overwritten in order to implement the actual measure.  As a parameter, a mapper object is provided that already refers to the corresponding bounding box and the corresponding time span.  If the begin of the time span is not provided, `2004-01-01T00:00Z` is automatically used as a default value.  The mapper can be used to filter and aggregate the data, as is described in the documentation of the [HeiGIT OSHDB](???).

In order to aggregate the data by grid cells, a function `gridCell` is provided that accepts as parameters a geometry, or a `OSHDBEntitySnapshot`.  Accordingly, the aggregation can either shortly be written as in the above example, or as follows in case it should be aggregated manually:

```java
                .aggregateBy(snapshot -> this.gridCell(snapshot.getGeometry()))
```
This way of aggregation is of particular interest when the data should not be aggregated by the centroid of the geometry, but by the first node of the geometry, by the centroid of the convex hull, etc.

## Instantiation of the Measure

In contrast to the class `Measure`, the constructor of the class `MeasureOSHDB` accepts an `OSHDB` object as a parameter.  A typical way of running the REST server is as follows:

```java
OSHDB_Database oshdb = new OSHDB_H2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements(oshdb));
restServer.run();
```

Instead of using only one database for the data as well as for the keytables, also two separate databases can be used (compare the documentation of the [HeiGIT OSHDB](???)):

```java
OSHDB_Database oshdb = new OSHDB_H2(...).multithreading(true);
OSHDB_JDBC oshdb_keydb = new OSHDB_H2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements(oshdb, oshdb_keydb));
```

## Author

This software is written and maintained by Franz-Benjamin Mocnik, <mocnik@uni-heidelberg.de>, GIScience Research Group, Institute of Geography, Heidelberg University.

The development has been supported by the DFG project *A framework for measuring the fitness for purpose of OpenStreetMap data based on intrinsic quality indicators* (FA 1189/3-1).

(c) by Heidelberg University, 2017.

## License

The code is licensed under the [MIT license](https://github.com/giscience/measures-rest-oshdb/blob/master/LICENSE.md).
