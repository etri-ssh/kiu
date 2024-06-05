package com.example.ccp.common.retrofit.model;

public class SessionLogRequest {
    public String usrId = "1";
    public String termId = "83282d64d886e359";
    public String crs = "5186";
    public Long rawBeginningDate;
    public Long rawEndDate;

    public SessionLogRequest() {}
    public SessionLogRequest(Long rawBeginningDate, Long rawEndDate) {
        this.rawBeginningDate = rawBeginningDate;
        this.rawEndDate = rawEndDate;
    }

    @Override
    public String toString() {
        return "SessionLogRequest{" +
            "usrId='" + usrId + '\'' +
            ", termId='" + termId + '\'' +
            ", crs='" + crs + '\'' +
            ", rawBeginningDate=" + rawBeginningDate +
            ", rawEndDate=" + rawEndDate +
            '}';
    }
}
