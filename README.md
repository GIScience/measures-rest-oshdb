# Measures REST OSHDB

The library `Measues REST OSHDB` provides an extension to the library [Measures REST](https://github.com/giscience/measures-rest).  It aids with implementing a measure that consumes data from the [OpenStreetMap History Database (OSHDB)](???).

## Implementing a Measure

A measure that consumes data from the [OSHDB](???) extends the class `MeasureOSHDB<R, O extends OSHDBMapReducible>`.  Here, `R` is a generic parameter that refers to the result of the measure; and `O` is the class to be mapped. As an example, one may extend the class `MeasureOSHDB` as follows:

```java
@Path("api/" + MeasureLengthOfElements.name)
public class MeasureLengthOfElements extends MeasureOSHDB<Number, OSMEntitySnapshot> {
    public static final String name = "measure-length-of-elements";

    public MeasureTest(OSHDBJdbc oshdb) {
        super(oshdb);
    }

    public MeasureTest(OSHDBDatabase oshdb, OSHDBJdbc oshdb_keydb) {
        super(oshdb, oshdb_keydb);
    }

    @Override
    public SortedMap<GridCell, Number> compute(MapAggregator<GridCell, OSMEntitySnapshot> mapReducer) throws Exception {
        return mapReducer
                .osmTag("highway", "residential")
                .osmTag("maxspeed")
                .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                .sum();
    }
}
```

Instead of the function `compute(BoundingBox bbox)`, the function `compute(MapAggregator<GridCell, O> mapReducer)` can be overwritten in order to implement the actual measure.  As a parameter, a mapReducer object is provided that already refers to the corresponding bounding box and the corresponding time span.  If the begin of the time span is not provided, `2004-01-01T00:00Z` is automatically used as a default value.  The mapReducer can be used to filter and aggregate the data, as is described in the documentation of the [OSHDB](???).

The data is automatically aggregated by the `MapAggregator`.  If, however, the data shall be aggregated manually, the method `gridCell` offers a simple way to aggregate manually.  It accepts either a `OSMEntitySnapshot`, a `OSMContribution`, or a geometry.  A geometry needs to be provided if different ways of aggregation are of interest, for example, when the data should not be aggregated by the centroid of the geometry but rather by the first node of the geometry, by the centroid of the convex hull, etc.

## Instantiation of the Measure

In contrast to the class `Measure`, the constructor of the class `MeasureOSHDB` accepts an `OSHDB` object as a parameter.  A typical way of running the REST server is as follows:

```java
OSHDBDatabase oshdb = new OSHDBH2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements(oshdb));
restServer.run();
```

Instead of using only one database for the data as well as for the keytables, also two separate databases can be used (compare the documentation of the [OSHDB](???)):

```java
OSHDBDatabase oshdb = new OSHDBH2(...).multithreading(true);
OSHDBJdbc oshdbKeydb = new OSHDBH2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements(oshdb, oshdbKeydb));
```

## Author

This software is written and maintained by Franz-Benjamin Mocnik, <mocnik@uni-heidelberg.de>, GIScience Research Group, Institute of Geography, Heidelberg University.

The development has been supported by the DFG project *A framework for measuring the fitness for purpose of OpenStreetMap data based on intrinsic quality indicators* (FA 1189/3-1).

(c) by Heidelberg University, 2017.

## License

The code is licensed under the [MIT license](https://github.com/giscience/measures-rest-oshdb/blob/master/LICENSE).
