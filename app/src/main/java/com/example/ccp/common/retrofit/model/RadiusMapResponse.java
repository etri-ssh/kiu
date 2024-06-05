package com.example.ccp.common.retrofit.model;

import java.util.List;

public class RadiusMapResponse {
    public int buildingId;
    public String buildingName;
    public String buildingAddress;
    public int totalFloors;
    public int baseFloors;
    public String buildingNote;
    public List<RadiusFloor> floor;
}
