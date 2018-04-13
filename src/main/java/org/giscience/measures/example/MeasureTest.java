package org.giscience.measures.example;

import org.giscience.measures.rest.measure.MeasureOSHDB;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;

import javax.ws.rs.Path;
import java.util.SortedMap;

@Path("api/" + MeasureTest.name)
public class MeasureTest extends MeasureOSHDB<Number, OSMEntitySnapshot> {
    public static final String name = "measure-test";

    public MeasureTest(OSHDBJdbc oshdb) {
        super(oshdb);
    }

    public MeasureTest(OSHDBDatabase oshdb, OSHDBJdbc oshdb_keydb) {
        super(oshdb, oshdb_keydb);
    }

    @Override
    public SortedMap<GridCell, Number> compute(MapReducer<OSMEntitySnapshot> mapReducer) throws Exception {
        return mapReducer
                .where("highway", "residential")
                .where("maxspeed")
                .aggregateBy(this::gridCell)
                .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                .sum();
    }
}
