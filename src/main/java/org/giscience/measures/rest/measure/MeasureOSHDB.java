package org.giscience.measures.rest.measure;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.giscience.measures.rest.utils.BoundingBox;
import org.giscience.utils.geogrid.geometry.GridCell;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.api.mapper.Mapper;
import org.heigit.bigspatialdata.oshdb.api.mapper.MapperFactory;
import org.heigit.bigspatialdata.oshdb.api.objects.Timestamps;

import java.lang.reflect.ParameterizedType;
import java.time.ZonedDateTime;
import java.util.SortedMap;

import static java.time.ZoneOffset.UTC;

/**
 *
 * @author Franz-Benjamin Mocnik
 */
public abstract class MeasureOSHDB<R, M extends MapperFactory, O> extends Measure<R> {
    private OSHDB _oshdb;
    private Class<M> _mapperClass;

    public MeasureOSHDB(OSHDB oshdb) {
        super();
        this._oshdb = oshdb;
        ParameterizedType parametrizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this._mapperClass = (Class) parametrizedType.getActualTypeArguments()[1];
    }

    @Override
    protected SortedMap<GridCell, R> compute(BoundingBox bbox, ZonedDateTime date, ZonedDateTime dateFrom) throws Exception {
        if (dateFrom == null) dateFrom = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, UTC);
        Mapper mapper = ((Mapper) this._mapperClass.getMethod("using", OSHDB.class).invoke(null, this._oshdb))
                .boundingBox(new org.heigit.bigspatialdata.oshdb.util.BoundingBox(bbox.minLon, bbox.maxLon, bbox.minLat, bbox.maxLat))
                .timestamps(new Timestamps(dateFrom.getYear(), date.getYear(), dateFrom.getMonthValue(), date.getMonthValue(), dateFrom.getDayOfMonth(), date.getDayOfMonth()));
        return this.compute(mapper);
    }

    public Pair<GridCell, R> handleGrid(Geometry g, R r) {
        try {
            return ImmutablePair.of(this._grid.cellForCentroid(g), r);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public abstract SortedMap<GridCell, R> compute(Mapper<O> mapper) throws Exception;
}
