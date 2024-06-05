package com.example.ccp.common.retrofit.model;

public class BLERequest {
    public Long rawScantime = System.currentTimeMillis();
    public Integer rssi;
    public String mac;

    public BLERequest(Integer rssi, String mac) {
        this.rssi = rssi;
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "BLERequest{" +
            "rawScantime=" + rawScantime +
            ", rssi=" + rssi +
            ", mac='" + mac + '\'' +
            '}';
    }
}
