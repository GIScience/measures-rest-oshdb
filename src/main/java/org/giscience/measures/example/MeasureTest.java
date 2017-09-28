package org.giscience.measures.example;

import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.measure.MeasureOSHDB;
import org.giscience.utils.geogrid.geometry.GridCell;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_JDBC;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.Geo;

import javax.ws.rs.Path;
import java.util.SortedMap;

@Path("api/" + MeasureTest.name)
public class MeasureTest extends MeasureOSHDB<Double, OSMEntitySnapshotView, OSMEntitySnapshot> {
    public static final String name = "measure-test";

    public MeasureTest(OSHDB_JDBC oshdb) {
        super(oshdb);
    }

    public MeasureTest(OSHDB oshdb, OSHDB_JDBC oshdb_keydb) {
        super(oshdb, oshdb_keydb);
    }

    @Override
    public SortedMap<GridCell, Double> compute(MapReducer<OSMEntitySnapshot> mapper) throws Exception {
        return mapper
                .filterByTag("highway", "residential")
                .filterByTag("maxspeed")
                .map(snapshot -> this.handleGrid(snapshot.getGeometry(), Geo.lengthOf(snapshot.getGeometry())))
                .aggregate(Pair::getKey)
                .map(Pair::getValue)
                .sum(n -> (Double)n);
    }
}
