package com.example.ccp.common.retrofit.model;

public class SignalResponse {
    public SignalBuilding building;
    private float x; // lon
    private float y; // lat
    public String message;
    public boolean indoor;

    public float getLon() { return this.x; }
    public float getLat() { return this.y; }

    @Override
    public String toString() {
        return "SignalResponse{" +
            "building=" + building +
            ", x=" + x +
            ", y=" + y +
            ", message='" + message + '\'' +
            ", indoor=" + indoor +
            '}';
    }
}
