package org.giscience.measures.example;

import org.giscience.measures.rest.measure.MeasureOSHDB;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;

import javax.ws.rs.Path;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.SortedMap;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;

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

    @Override
    public SortedMap<GridCell, Number> compute(MapAggregator<GridCell, OSMEntitySnapshot> mapReducer, RequestParameter p) throws Exception {
        return mapReducer
                .osmTag("highway", "residential")
                .osmTag("maxspeed")
                .map(snapshot -> Geo.lengthOf(snapshot.getGeometry()))
                .sum();
    }
}
