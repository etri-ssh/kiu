package com.example.ccp.common.retrofit.model;

public class LTERequest {
    public Long rawScantime = System.currentTimeMillis();
    public String mnc;
    public Integer rsrq;
    public Integer pci;
    public Integer rsrp;
    public Integer band;
    public String mcc;
    public Integer ta;
    public Integer cid;

    public LTERequest(String mnc, Integer rsrq, Integer pci, Integer rsrp, Integer band, String mcc, Integer ta, Integer cid) {
        this.mnc = mnc;
        this.rsrq = rsrq;
        this.pci = pci;
        this.rsrp = rsrp;
        this.band = band;
        this.mcc = mcc;
        this.ta = ta;
        this.cid = cid;
    }

    @Override
    public String toString() {
        return "LTERequest{" +
            "rawScantime=" + rawScantime +
            ", mnc='" + mnc + '\'' +
            ", rsrq=" + rsrq +
            ", pci=" + pci +
            ", rsrp=" + rsrp +
            ", band=" + band +
            ", mcc='" + mcc + '\'' +
            ", ta=" + ta +
            ", cid=" + cid +
            '}';
    }
}
