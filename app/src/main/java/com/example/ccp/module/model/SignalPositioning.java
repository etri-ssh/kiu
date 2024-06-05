package com.example.ccp.module.model;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.SignalResponse;

public class SignalPositioning extends BasePositioning {
    public double floor = 0;
    public String msg = "Success";

    public SignalPositioning(SignalResponse location, int flag) {
        super(Common.T_SIGNAL, location.getLon(), location.getLat(), flag);
        this.floor = location.building.floor.floorNumber;
    }

    public SignalPositioning(String msg) { super(Common.T_FUSED, 0.0, 0.0, 0); this.msg = msg; }

    @NonNull
    @Override
    public String toString() {
        return "SignalPositioning{" +
            "floor=" + floor +
            ", type='" + type + '\'' +
            ", lon=" + lon +
            ", lat=" + lat +
            ", flag=" + flag +
            ", msg=" + msg +
            '}';
    }
}
