package com.example.sensorlog.scanner.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.logger.LocationLogger;
import com.example.sensorlog.scanner.sensor.Glob_Pos;

@SuppressLint({"MissingPermission", "InlinedApi"})
public class FlpLScanner extends BaseScanner implements LocationListener {
    private final LocationManager locationManager;
    private final LocationLogger locationLogger;

    public FlpLScanner(Context context, String header, LocationLogger locationLogger) {
        super(header);
        this.locationLogger = locationLogger;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fileStream = new FileStream("FLP");
    }

    @Override
    public void start() {
      //  if(!activation) {
            super.start();
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 500, 0, this);
      //  }
    }

    @Override
    public void stop() {
        super.stop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        double Lon,Lat;

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

 //       if(activation) locationLogger.update(location.getProvider(), location);
    }
}
