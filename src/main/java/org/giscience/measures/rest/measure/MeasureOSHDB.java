package org.giscience.measures.rest.measure;

import com.vividsolutions.jts.geom.Geometry;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.cells.GridCell;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.SortedMap;

import static java.time.ZoneOffset.UTC;

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
    protected SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom) throws Exception {
        if (dateFrom == null) dateFrom = ZonedDateTime.of(2004, 1, 1, 0, 0, 0, 0, UTC);
        MapReducer<O> mapper = (MapReducer) this._oshdb.createMapReducer(this._mapperClass)
                .keytables(this._oshdb_keydb)
                .areaOfInterest(new org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox(bbox.minLon, bbox.minLat, bbox.maxLon, bbox.maxLat))
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

    public abstract SortedMap<GridCell, R> compute(MapReducer<O> mapReducer) throws Exception;
}
