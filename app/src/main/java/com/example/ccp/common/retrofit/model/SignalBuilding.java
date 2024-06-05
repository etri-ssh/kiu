package com.example.ccp.common.retrofit.model;

public class SignalBuilding {
    public Integer buildingId;
    public String buildingName;
    public String address;
    public Integer totalFloors;
    public Integer baseFloors;
    public String buildingExternalId;
    public SignalFloor floor;

    @Override
    public String toString() {
        return "SignalBuilding{" +
            "buildingId=" + buildingId +
            ", buildingName='" + buildingName + '\'' +
            ", address='" + address + '\'' +
            ", totalFloors=" + totalFloors +
            ", baseFloors=" + baseFloors +
            ", buildingExternalId=" + buildingExternalId +
            ", floor=" + floor +
            '}';
    }
}
