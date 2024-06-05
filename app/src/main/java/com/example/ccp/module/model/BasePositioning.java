package com.example.ccp.module.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.module.pos.FusedModule;

public class BasePositioning {
    public String type;
    public double lon;
    public double lat;
    public int flag;
    public int ppsPos;
    public int sigPos;

    public BasePositioning(String type, double lon, double lat, int flag) {
        this.type = type;
        this.lon = lon;
        this.lat = lat;
        this.flag = flag;
    }

    public BasePositioning(String type, Location location) {
        this.type = type;
        this.lon = location.getLongitude();
        this.lat = location.getLatitude();
        this.flag = 1;
    }

    public BasePositioning(int ppsPos, int sigPos) {
        this.type = Common.T_FUSED;
        this.lon = FusedModule.location.getLongitude();
        this.lat = FusedModule.location.getLatitude();
        this.flag = 0;
        this.ppsPos = ppsPos;
        this.sigPos = sigPos;
    }

    public BasePositioning(String type) { this.type = type; }

    @NonNull
    @Override
    public String toString() {
        return "BasePositioning{" +
            "type='" + type + '\'' +
            ", lon=" + lon +
            ", lat=" + lat +
            ", flag=" + flag +
            ", ppsPos=" + ppsPos +
            ", sigPos=" + sigPos +
            '}';
    }
}
