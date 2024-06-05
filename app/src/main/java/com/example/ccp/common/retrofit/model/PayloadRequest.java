package com.example.ccp.common.retrofit.model;

import java.util.List;

public class PayloadRequest {
    public List<BLERequest> ble;
    public List<LTERequest> lte;
    public List<WIFIRequest> wifi;

    public PayloadRequest(List<BLERequest> ble, List<LTERequest> lte, List<WIFIRequest> wifi) {
        this.ble = ble;
        this.lte = lte;
        this.wifi = wifi;
    }

    @Override
    public String toString() {
        return "PayloadRequest{" +
            "ble=" + ble +
            ", lte=" + lte +
            ", wifi=" + wifi +
            '}';
    }
}
