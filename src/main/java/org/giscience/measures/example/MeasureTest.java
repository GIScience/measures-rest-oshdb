package org.giscience.measures.example;

import org.giscience.measures.rest.measure.MeasureOSHDB;
import org.giscience.utils.geogrid.geometry.GridCell;
import org.heigit.bigspatialdata.oshdb.OSHDB;
import org.heigit.bigspatialdata.oshdb.api.mapper.Mapper;
import org.heigit.bigspatialdata.oshdb.api.mapper.OSMEntitySnapshotMapper;
import org.heigit.bigspatialdata.oshdb.api.objects.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.util.Geo;

import javax.ws.rs.Path;
import java.util.SortedMap;

@Path("api/" + MeasureTest.name)
public class MeasureTest extends MeasureOSHDB<Double, OSMEntitySnapshotMapper, OSMEntitySnapshot> {
    public static final String name = "measure-test";

    public MeasureTest(OSHDB oshdb) {
        super(oshdb);
    }

    @Override
    public SortedMap<GridCell, Double> compute(Mapper<OSMEntitySnapshot> mapper) throws Exception {
        return mapper
                .filterByTagValue("highway", "residential")
                .filterByTagKey("maxspeed")
                .sumAggregate(snapshot -> this.handleGrid(snapshot.getGeometry(), Geo.lengthOf(snapshot.getGeometry())));
    }
}
