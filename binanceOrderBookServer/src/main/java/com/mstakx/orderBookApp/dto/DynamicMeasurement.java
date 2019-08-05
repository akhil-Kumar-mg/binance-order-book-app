package com.mstakx.orderBookApp.dto;

import org.influxdb.annotation.Measurement;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

public class DynamicMeasurement implements Measurement {

    private String name;

    public DynamicMeasurement(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String database() {
        return "[unassigned]";
    }

    @Override
    public String retentionPolicy() {
        return "autogen";
    }

    @Override
    public TimeUnit timeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DynamicMeasurement.class;
    }
}
