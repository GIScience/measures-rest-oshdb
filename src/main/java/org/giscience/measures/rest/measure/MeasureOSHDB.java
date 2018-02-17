package org.giscience.measures.rest.measure;

import com.vividsolutions.jts.geom.Geometry;
import org.giscience.measures.rest.utils.BoundingBox;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_JDBC;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapperFactory;
import org.heigit.bigspatialdata.oshdb.api.objects.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;

import static java.time.ZoneOffset.UTC;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class MeasureOSHDB<R, M extends MapperFactory, O> extends Measure<R> {
    private OSHDB _oshdb;
    private OSHDB_JDBC _oshdb_keydb;
    private Class<M> _mapperClass;

    public MeasureOSHDB(OSHDB_JDBC oshdb) {
        this(oshdb, oshdb);
    }

    public MeasureOSHDB(OSHDB oshdb, OSHDB_JDBC oshdb_keydb) {
        super();
        this._oshdb = oshdb;
        this._oshdb_keydb = oshdb_keydb;
        ParameterizedType parametrizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this._mapperClass = (Class) parametrizedType.getActualTypeArguments()[1];
    }

    @Override
    protected SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom) throws Exception {
        if (dateFrom == null) dateFrom = ZonedDateTime.of(2004, 1, 1, 0, 0, 0, 0, UTC);
        MapReducer mapper = ((MapReducer) this._mapperClass.getMethod("on", OSHDB.class).invoke(null, this._oshdb))
                .keytables(this._oshdb_keydb)
                .areaOfInterest(new org.heigit.bigspatialdata.oshdb.util.BoundingBox(bbox.minLon, bbox.maxLon, bbox.minLat, bbox.maxLat))
                .timestamps(dateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE), date.format(DateTimeFormatter.ISO_LOCAL_DATE), OSHDBTimestamps.Interval.MONTHLY);
        return this.compute(mapper);
    }

    public GridCell gridCell(Geometry g) {
        try {
            return this._grid.cellForCentroid(g);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public GridCell gridCell(OSMEntitySnapshot snapshot) {
        return this.gridCell(snapshot.getGeometry());
    }

    public abstract SortedMap<GridCell, R> compute(MapReducer<O> mapper) throws Exception;
}
