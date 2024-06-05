package com.example.ccp.common.retrofit.model;

import java.util.ArrayList;
import java.util.List;

public class SignalRequest {
    public SessionLogRequest sessionLog = new SessionLogRequest();
    public List<PayloadRequest> payload = new ArrayList<>();

    @Override
    public String toString() {
        return "SignalRequest{" +
            "sessionLog=" + sessionLog +
            ", payload=" + payload +
            '}';
    }
}
