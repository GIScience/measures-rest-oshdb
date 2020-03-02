package org.giscience.measures.rest.measure;

import org.giscience.measures.rest.server.OSHDBRequestParameter;
import org.giscience.measures.rest.server.RequestParameter;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.locationtech.jts.geom.Geometry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    private OSHDBDatabase _oshdb = null;
    private OSHDBJdbc _oshdb_keydb = null;
    private Class<O> _mapperClass = null;

    protected MeasureOSHDB() {
        super();
    }

    public MeasureOSHDB<R, O> setOSHDB(OSHDBJdbc oshdb) {
        return this.setOSHDB(oshdb, oshdb);
    }

    public MeasureOSHDB<R, O> setOSHDB(OSHDBDatabase oshdb, OSHDBJdbc oshdb_keydb) {
        this._oshdb = oshdb;
        this._oshdb_keydb = oshdb_keydb;
        Class<?> superClass = getClass();
        Type superType;
        do {
            superType = superClass.getGenericSuperclass();
            superClass = (Class<?>) ((superType instanceof Class<?>) ? superType : ((ParameterizedType) superType).getRawType());
        } while (!(superClass.equals(MeasureOSHDB.class)));
        this._mapperClass = (Class) ((ParameterizedType) superType).getActualTypeArguments()[1];
        return this;
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
        if (this._oshdb == null) throw new Exception("Measure not initialized.  Please provide information about hte OSHDB by using setOSHDB.");
        if (dateFrom == null) dateFrom = date;
        MapReducer mapReducer = this._oshdb.createMapReducer(this._mapperClass)
                .keytables(this._oshdb_keydb)
                .areaOfInterest(new OSHDBBoundingBox(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat))
                .timestamps(new OSHDBTimestamps(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE), date.format(DateTimeFormatter.ISO_LOCAL_DATE), String.format("P%0$dD", intervalInDays), true));
        MapAggregator mapReducer2;
        if (this._mapperClass == OSMEntitySnapshot.class) mapReducer2 = mapReducer.aggregateBy(o -> this.gridCell((OSMEntitySnapshot) o));
        else if (this._mapperClass == OSMContribution.class) mapReducer2 = mapReducer.aggregateBy(o -> this.gridCell((OSMContribution) o));
        else mapReducer2 = mapReducer.aggregateBy(o -> null);
        return this.compute(mapReducer2, new OSHDBRequestParameter(p));
    }

    public GridCell gridCell(OSMEntitySnapshot snapshot) {
        return this.gridCell(snapshot.getGeometry());
    }

    public GridCell gridCell(OSMContribution contribution) {
        Geometry g = contribution.getGeometryUnclippedAfter();
        if (g == null || g.isEmpty()) g = contribution.getGeometryUnclippedBefore();
        if (g == null || g.isEmpty()) throw new RuntimeException("missing geometry for " + contribution.getOSHEntity().getId());
        return this.gridCell(g);
    }

    public GridCell gridCell(Geometry g) {
        try {
            return this._grid.cellForCentroid(g);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected OSHDBDatabase getOSHDB() {
        return this._oshdb;
    }

    public abstract SortedMap<GridCell, R> compute(MapAggregator<GridCell, O> mapReducer, OSHDBRequestParameter p) throws Exception;
}
