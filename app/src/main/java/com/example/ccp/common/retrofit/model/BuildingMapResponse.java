package com.example.ccp.common.retrofit.model;

import com.example.ccp.common.Common;

import org.json.JSONObject;

public class BuildingMapResponse {
    public int floorId;
    public String floorName;
    public double floorNumber;
    private String backgroundMapGeometry;
    private String pathGeometry;

    private JSONObject jsonBackgroundMapGeometry;
    private JSONObject jsonPathGeometry;

    public boolean isBackgroundMapGeometry() {
        if(backgroundMapGeometry == null) return false;
        else return getBackgroundMapGeometry() != null;
    }

    public JSONObject getBackgroundMapGeometry() {
        if(backgroundMapGeometry == null) return null;
        else {
            try {
                if(jsonBackgroundMapGeometry == null) jsonBackgroundMapGeometry = new JSONObject(backgroundMapGeometry);
            } catch(Exception e) { Common.logW("getBackgroundMapGeometry Error : " + e); }
            return jsonBackgroundMapGeometry;
        }
    }

    public boolean isPathGeometry() {
        if(pathGeometry == null) return false;
        else return getPathGeometry() != null;
    }

    public JSONObject getPathGeometry() {
        if(pathGeometry == null) return null;
        else {
            try {
                if(jsonPathGeometry == null) jsonPathGeometry = new JSONObject(pathGeometry);
            } catch(Exception e) { Common.logW("getPathGeometry Error : " + e); }
            return jsonPathGeometry;
        }
    }
}
