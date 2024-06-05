package com.example.ccp.common.retrofit.model;

import java.io.Serializable;

public class BuildingResponse implements Serializable {
    public int id;
    public String name;
    public String address;
    public int totalFloors;
    public int baseFloors;
    public String registrationDate;
    public String updatedDate;
    public String note;
}
