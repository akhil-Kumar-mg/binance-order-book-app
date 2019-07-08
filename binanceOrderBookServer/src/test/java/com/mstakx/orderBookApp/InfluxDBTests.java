package com.mstakx.orderBookApp;

import com.mstakx.orderBookApp.measurement.Memory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.*;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InfluxDBTests {
    @Test
    public void whenCorrectInfoDatabaseConnects() {

        InfluxDB connection = connectDatabase();
        assertTrue(pingServer(connection));
    }

    private InfluxDB connectDatabase() {

        // Connect to database assumed on localhost with default credentials.
        return  InfluxDBFactory.connect("http://127.0.0.1:8086", "admin", "admin");

    }

    private boolean pingServer(InfluxDB influxDB) {
        try {
            // Ping and check for version string
            Pong response = influxDB.ping();
            if (response.getVersion().equalsIgnoreCase("unknown")) {
                return false;
            } else {
                return true;
            }
        } catch (InfluxDBIOException idbo) {
            return false;
        }
    }

    @Test
    public void whenDatabaseCreatedDatabaseChecksOk() {

        InfluxDB connection = connectDatabase();

        connection.createDatabase("testDB");
        assertTrue(connection.databaseExists("testDB"));

        // Verify that nonsense databases are not there
        assertFalse(connection.databaseExists("noDB"));

        connection.deleteDatabase("testDB");
        assertFalse(connection.databaseExists("testDB"));
    }

    @Test
    public void whenPointsWrittenPointsExists() throws Exception {

        InfluxDB connection = connectDatabase();

        String dbName = "testDB";
        connection.createDatabase(dbName);

        // Need a retention policy before we can proceed
        // Since we are doing batches, we need not set it
        connection.createRetentionPolicy("defaultPolicy", "testDB", "30d", 1, true);


        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("defaultPolicy")
                .build();
        Point point1 = Point.measurement("memory")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("free", 4743656L)
                .addField("used", 1015096L)
                .addField("buffer", 1010467L)
                .build();
        Point point2 = Point.measurement("memory")
                .time(System.currentTimeMillis() - 100, TimeUnit.MILLISECONDS)
                .addField("free", 4743696L)
                .addField("used", 1016096L)
                .addField("buffer", 1008467L)
                .build();
        batchPoints.point(point1);
        batchPoints.point(point2);
        connection.write(batchPoints);

        List<Memory> memoryPointList = getPoints(connection, "Select * from memory", "testDB");

        assertEquals(2, memoryPointList.size());
        assertTrue(4743696L == memoryPointList.get(0).getFree());


        memoryPointList = getPoints(connection, "Select * from memory order by time desc", "testDB");

        assertEquals(2, memoryPointList.size());
        assertTrue(4743656L == memoryPointList.get(0).getFree());

        connection.query(new Query("DROP DATABASE testDB", "testDB"));
        connection.close();
    }

    private List<Memory> getPoints(InfluxDB connection, String query, String databaseName) {

        // Run the query
        Query queryObject = new Query(query, databaseName);
        QueryResult queryResult = connection.query(queryObject);

        // Map it
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, Memory.class);
    }
}
