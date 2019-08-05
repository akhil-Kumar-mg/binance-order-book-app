package com.mstakx.orderBookApp.config;

import com.mstakx.orderBookApp.exception.DBConnectionFailureException;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("InfluxDbConfig")
public class InfluxDbConfig {

    @Value("${spring.influx.url}")
    private String influxDbUrl;

    @Value("${spring.influx.username}")
    private String influxDbUsername;

    @Value("${spring.influx.password}")
    private String influxDbPassword;

    private static InfluxDB influxDB;

    /*
      Creates the influx DB connection
     */
    public InfluxDB createConnection() {
        influxDB = InfluxDBFactory.connect(influxDbUrl, influxDbUsername, influxDbPassword);

        try {
            influxDB.ping();
        } catch (Exception ex) {
            throw new DBConnectionFailureException("DB Connection Failed", ex);
        }

        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        return influxDB;
    }

    public static InfluxDB getConnection() {
        return influxDB;
    }

}
