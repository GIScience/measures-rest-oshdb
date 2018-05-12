package org.giscience.measures.rest.measure;

import com.vividsolutions.jts.geom.Geometry;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.SortedMap;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class MeasureOSHDB<R, O extends OSHDBMapReducible> extends Measure<R> {
    private OSHDBDatabase _oshdb;
    private OSHDBJdbc _oshdb_keydb;
    private Class<O> _mapperClass;

    public MeasureOSHDB(OSHDBJdbc oshdb) {
        this(oshdb, oshdb);
    }

    public MeasureOSHDB(OSHDBDatabase oshdb, OSHDBJdbc oshdb_keydb) {
        super();
        this._oshdb = oshdb;
        this._oshdb_keydb = oshdb_keydb;
        ParameterizedType parametrizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this._mapperClass = (Class) parametrizedType.getActualTypeArguments()[1];
    }

    @Override
    public ZonedDateTime defaultDate() {
//        TODO
//        System.out.println(this._oshdb.metadata("date"));
        return ZonedDateTime.now(UTC).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(DAYS);
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
    protected SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom, Integer intervalInDays, RequestParameter p) throws Exception {
        if (dateFrom == null) dateFrom = date;
        MapAggregator mapReducer = this._oshdb.createMapReducer(this._mapperClass)
                .keytables(this._oshdb_keydb)
                .areaOfInterest(new OSHDBBoundingBox(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat))
                .timestamps(new OSHDBTimestamps(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE), date.format(DateTimeFormatter.ISO_LOCAL_DATE), String.format("P%0$dD", intervalInDays), true))
                .aggregateBy(o -> {
                    if (this._mapperClass == OSMEntitySnapshot.class) return this.gridCell((OSMEntitySnapshot) o);
                    if (this._mapperClass == OSMContribution.class) return this.gridCell((OSMContribution) o);
                    return null;
                });
        return this.compute(mapReducer, p);
    }

    public GridCell gridCell(OSMEntitySnapshot snapshot) {
        return this.gridCell(snapshot.getGeometry());
    }

    public GridCell gridCell(OSMContribution contribution) {
        return this.gridCell(contribution.getGeometryBefore());
    }

    public GridCell gridCell(Geometry g) {
        try {
            return this._grid.cellForCentroid(g);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract SortedMap<GridCell, R> compute(MapAggregator<GridCell, O> mapReducer, RequestParameter p) throws Exception;
}
