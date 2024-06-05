package com.example.ccp.common.retrofit.model;

public class SignalFloor {
    public Integer floorId;
    public Double floorNumber;
    public String floorName;

    @Override
    public String toString() {
        return "SignalFloor{" +
            "floorId=" + floorId +
            ", floorNumber=" + floorNumber +
            ", floorName='" + floorName + '\'' +
            '}';
    }
}
