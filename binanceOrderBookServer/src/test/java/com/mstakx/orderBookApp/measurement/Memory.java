package com.mstakx.orderBookApp.measurement;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name ="memory")
public class Memory {

    @Column(name = "time")
    private Instant time;

    @Column(name = "name")
    private String name;

    @Column(name = "free")
    private Long free;

    @Column(name = "used")
    private Long used;

    @Column(name = "buffer")
    private Long buffer;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFree() {
        return free;
    }

    public void setFree(Long free) {
        this.free = free;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public Long getBuffer() {
        return buffer;
    }

    public void setBuffer(Long buffer) {
        this.buffer = buffer;
    }
}
