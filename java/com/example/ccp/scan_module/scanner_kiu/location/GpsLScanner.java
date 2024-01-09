package com.example.sensorlog.scanner.location;

import static com.example.sensorlog.MainActivity.PDR_ON_OFF;
import static com.example.sensorlog.MainActivity.time_s;

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
import com.example.sensorlog.scanner.sensor.Lowpassfilter;

import java.util.ArrayList;

@SuppressLint({"MissingPermission", "InlinedApi"})
public class GpsLScanner extends BaseScanner implements LocationListener {
    private final LocationManager locationManager;
    private final LocationLogger locationLogger;
    private ArrayList<Integer> IO = new ArrayList<>();
    public static int G_P = 0;  // Measure 동기화

    public static double G_VDOP;  //VDOP 값
    public static int G_IO = 1 ;  //현제 실내/외 확인
    public static double G_IO2 = 1 ; //초기 실내/외 확인
    public GpsLScanner(Context context, String header, LocationLogger locationLogger) {
        super(header);
        this.locationLogger = locationLogger;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fileStream = new FileStream("GPS");
    }

    @Override
    public void start() {
     //   if(!activation) {
            super.start();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    //    }
    }

    @Override
    public void stop() {
        super.stop();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double Lon, Lat;


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) data += "," + location.isMock();


        Lat = location.getLatitude();
        Lon = location.getLongitude();
        Glob_Pos.Get_GPS(Lat, Lon);

        G_P = 1;
        G_VDOP = location.getVerticalAccuracyMeters();
        if (location.getVerticalAccuracyMeters() < 2.55) {
            G_IO = 0;
        } else {
            G_IO = 1;
        }
        if(rowCount>10)
        {
        if (time_s == 0) {
            IO.add(G_IO);
        } else {
            G_IO2 = Lowpassfilter.mean_I(IO);
        }
    }
            data += "," + G_IO;
      //  }


        fileStream.fileWrite(data);
     //   if(activation) locationLogger.update(location.getProvider(), location);
    }
}
