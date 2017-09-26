# Measures REST OSHDB

The library `Measues REST OSHDB` provides an extension to the library [Measures REST](https://github.com/giscience/measures-rest).  It aids with implementing a measure that consumes data from the [HeiGIT OSHDB](???).

## Implementing a Measure

A measure that consumes data from the [HeiGIT OSHDB](???) should extend the class `MeasureOSHDB<R, M extends MapperFactory, O>`.  Here, `R` is a generic parameter that refers to the result of the measure; `M` a parameter that refers to which MapperFactory should be used; and `O` the class to be mapped. As an example, one may extend the class `MeasureOSHDB` as follows:

```java
@Path("api/" + MeasureLengthOfElements.name)
public class MeasureLengthOfElements extends MeasureOSHDB<Double, OSMEntitySnapshotMapper, OSMEntitySnapshot> {
    public static final String name = "measure-length-of-elements";

    @Override
    public SortedMap<GridCell, Double> compute(Mapper<OSMEntitySnapshot> mapper) throws Exception {
        return mapper
                .filterByTagValue("highway", "residential")
                .filterByTagKey("maxspeed")
                .sumAggregate(snapshot -> this.handleGrid(snapshot.getGeometry(), Geo.lengthOf(snapshot.getGeometry())));
    }
}
```

Instead of the function `compute(BoundingBox bbox)`, a new function `compute(Mapper<O> mapper)` can be overwritten in order to implement the actual measure.  As a parameter, a mapper object is provided that already refers to the corresponding bounding box and the corresponding time span.  If the begin of the time span is not provided, `1900-01-01T00:00Z` is automatically used as a default value.  The mapper can be used to filter and aggregate the data, as is described in the documentation of the [HeiGIT OSHDB](???).  In order to easily aggregate the data by grid cells, a function `handleGrid` is provided that accepts as parameters a geometry and a value.  The function returns an `ImmutablePair`, like is required for the function `sumAggregate`.

## Instantiation the Measure

In contrast to the class `Measure`, the constructor of the class `MeasureOSHDB` accepts an `OSHDB` object as a parameter.  A typical way of running the REST server is as follows:

```java
OSHDB oshdb = new OSHDB_H2(...);
RestServer restServer = new RestServer();
restServer.register(new MeasureLengthOfElements(oshdb));
restServer.run();
```

## Author

This software is written and maintained by Franz-Benjamin Mocnik, <mocnik@uni-heidelberg.de>, GIScience Research Group, Institute of Geography, Heidelberg University.

The development has been supported by the DFG project *A framework for measuring the fitness for purpose of OpenStreetMap data based on intrinsic quality indicators* (FA 1189/3-1).

(c) by Heidelberg University, 2017.

## License

The code is licensed under the [MIT license](https://github.com/giscience/measures-rest-oshdb/blob/master/LICENSE.md).
