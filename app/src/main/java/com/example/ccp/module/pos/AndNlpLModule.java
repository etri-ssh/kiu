package com.example.ccp.module.pos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.BasePositioning;

import java.util.ArrayList;

@SuppressLint({"MissingPermission", "InlinedApi"})
public class AndNlpLModule implements LocationListener {

    private final LocationManager locationManager;

    private int rowCount = 0;

    private ArrayList<Integer> IO = new ArrayList<>();

    public static Location location;

    public AndNlpLModule(Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start(){
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,this);
    }

    public void stop(){
        locationManager.removeUpdates(this);
    }

    public void onLocationChanged(@NonNull Location location) {

        /** [ETRI] DataCollection에 위치 결과만을를 추가 (24.01.29) **/
        Common.log("[AndGnssL] location : " + location);
        AndGnssLModule.location = location;
        DataCollection.instance.andNlpLPut(new BasePositioning(Common.T_ANDNLPL, location));

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

    }

}
