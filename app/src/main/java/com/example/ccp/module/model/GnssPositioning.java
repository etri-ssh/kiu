package com.example.ccp.module.model;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.ppsoln.apolo_r_lib.APOLO_R_Client.VO.EstimationResult;

public class GnssPositioning extends BasePositioning {
    public String msg = "Success";

    public GnssPositioning(EstimationResult location, int flag) {
        super(Common.T_GNSS, Double.parseDouble(location.getLongitude()), Double.parseDouble(location.getLatitude()), flag);
        if(lon == 0 || lat == 0) this.flag = 0;
    }

    public GnssPositioning(String msg) { super(Common.T_FUSED, 0.0, 0.0, 0); this.msg = msg; }

    @NonNull
    @Override
    public String toString() {
        return "GnssPositioning{" +
            "type='" + type + '\'' +
            ", lon=" + lon +
            ", lat=" + lat +
            ", flag=" + flag +
            ", msg=" + msg +
            '}';
    }
}
