package org.giscience.measures.example;

import org.giscience.measures.rest.server.RestServer;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;

public class ExampleOSHDB {
    public static void main(String[] args) throws Exception {
        OSHDBH2 oshdb = new OSHDBH2("./sweden.oshdb").multithreading(true);
        RestServer restServer = new RestServer();
        restServer.register(new MeasureTest(oshdb));
        restServer.register(new MeasureSaturation(oshdb));
        restServer.run();
    }
}
