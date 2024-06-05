package com.example.ccp.common.retrofit.model;

public class WIFIRequest {
    public Long rawScantime = System.currentTimeMillis();
    public Integer rssi;
    public String ssid;
    public String mac;

    public WIFIRequest(Integer rssi, String ssid, String mac) {
        this.rssi = rssi;
        this.ssid = ssid;
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "WIFIRequest{" +
            "rawScantime=" + rawScantime +
            ", rssi=" + rssi +
            ", ssid='" + ssid + '\'' +
            ", mac='" + mac + '\'' +
            '}';
    }
}
