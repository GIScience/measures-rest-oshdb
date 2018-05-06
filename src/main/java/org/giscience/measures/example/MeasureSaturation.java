package org.giscience.measures.example;

import org.giscience.measures.rest.measure.MeasureOSHDB;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.tools.Index;
import org.giscience.measures.tools.Lineage;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;

import javax.ws.rs.Path;
import java.util.SortedMap;

@Path("api/" + MeasureSaturation.name)
public class MeasureSaturation extends MeasureOSHDB<Number, OSMEntitySnapshot> {
    public static final String name = "measure-saturation";

    public MeasureSaturation(OSHDBJdbc oshdb) {
        super(oshdb);
    }

    public MeasureSaturation(OSHDBDatabase oshdb, OSHDBJdbc oshdb_keydb) {
        super(oshdb, oshdb_keydb);
    }

    @Override
    public Boolean refersToTimeSpan() {
        return true;
    }

    @Override
    public SortedMap<GridCell, Number> compute(MapAggregator<GridCell, OSMEntitySnapshot> mapReducer, RequestParameter p) throws Exception {
        return Index.reduce(
                mapReducer
                        .osmTag("highway", "residential")
                        .aggregateByTimestamp(snapshot -> snapshot.getTimestamp())
                        .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                        .sum(),
                Lineage::saturation
        );
    }
}
