package com.example.ccp.module.pos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.example.ccp.common.Common;
import com.example.ccp.common.FileStream;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.BasePositioning;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;

@SuppressLint("MissingPermission")
public class FusedModule {
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Context context;
    private int rowCount = 0;
    private final Timer timer = new Timer();

    public static Location location;

    private FileStream fileStream;

    public FusedModule(Context context) {

        fileStream = new FileStream("andFusedPos");
        fileStream.fileCreate(Common.LOC_TITLE); // 파일 로그 기능은 우선 삭제

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.context = context;
        start(Common.FUSED); // 주기 설정에 따라 변경 가능
    }

    private void start(long ms) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {
                Common.log("[fused] location : " + location);
                FusedModule.location = location;
                DataCollection.instance.fusedPut(new BasePositioning(Common.T_FUSED, location));

                String data = "\n" + (++rowCount) + "";
                data += "," + System.currentTimeMillis();
                data += "," + location.getElapsedRealtimeNanos();
                data += "," + location.getTime();
                data += "," + location.getProvider();
                data += "," + location.getLatitude();
                data += "," + location.getLongitude();
                data += "," + location.getAltitude();
                data += "," + location.getSpeed();
                data += "," + location.getAccuracy();
                data += "," + location.getBearing();
                data += "," + location.getSpeedAccuracyMetersPerSecond();
                data += "," + location.getBearingAccuracyDegrees();
                data += "," + location.getVerticalAccuracyMeters();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) data += "," + location.isMock();

                fileStream.fileWrite(data);

                });
            }
        }, 0, ms);
    }

    public void stop() {
        try {
            timer.cancel();
            fileStream.fileClose();
        }
        catch(Exception e) {
            Common.logW("[fused] start timer error");
            e.printStackTrace();
        }
    }
}