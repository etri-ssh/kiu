package com.example.sensorlog.scanner.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.logger.StatusLogger;

@SuppressLint("MissingPermission")
public class StatusScanner extends BaseScanner implements LocationListener {
    private final LocationManager locationManager;
    private final StatusLogger statusLogger;

    public StatusScanner(Context context, String header, StatusLogger statusLogger) {
        super(header);
        this.statusLogger = statusLogger;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fileStream = new FileStream("Status");
    }


    @Override
    public void start() {
  //      if(!activation) {
            super.start();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            locationManager.registerGnssStatusCallback(callback);
  //      }
    }

    @Override
    public void stop() {
        super.stop();
        locationManager.removeUpdates(this);
        locationManager.unregisterGnssStatusCallback(callback);
    }

    private final GnssStatus.Callback callback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            int count = status.getSatelliteCount();
            for(int i = 0; i < count; i++) {
                String data = "\n" + (++rowCount) + "";
                data += "," + System.currentTimeMillis();
                data += "," + count;
                data += "," + i;
                data += "," + status.getConstellationType(i);
                data += "," + status.getSvid(i);
                data += "," + status.getCarrierFrequencyHz(i);
                data += "," + status.getCn0DbHz(i);
                data += "," + status.getAzimuthDegrees(i);
                data += "," + status.getElevationDegrees(i);
                data += "," + status.usedInFix(i);
                data += "," + status.hasAlmanacData(i);
                data += "," + status.hasEphemerisData(i);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += "," + status.getBasebandCn0DbHz(i);
                fileStream.fileWrite(data);
            }
   //         if(activation) statusLogger.update("status", status);
        }
    };

    @Override
    public void onLocationChanged(@NonNull Location location) {}
}
