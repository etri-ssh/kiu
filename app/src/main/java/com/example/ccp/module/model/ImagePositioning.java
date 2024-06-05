package com.example.ccp.module.model;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.CameraResponse;

public class ImagePositioning extends BasePositioning {
    public int mode;
    public String msg = "Success";

    public ImagePositioning(CameraResponse cr) {
        super(Common.T_IMAGE);
        if(cr.mode == 1) {
            this.lon = cr.landmark_x;
            this.lat = cr.landmark_y;
        } else {
            this.lon = cr.camera_x;
            this.lat = cr.camera_y;
        }
        this.mode = cr.mode;
    }

    public ImagePositioning(String msg) { super(Common.T_FUSED, 0.0, 0.0, 0); this.msg = msg; }

    @NonNull
    @Override
    public String toString() {
        return "ImagePositioning{" +
            "mode=" + mode +
            ", type='" + type + '\'' +
            ", lon=" + lon +
            ", lat=" + lat +
            '}';
    }
}
