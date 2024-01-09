package com.example.sensorlog.common;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class Common {
    private final static String TAG = "cubicinc";
    public static void log(String msg) { Log.d(TAG, msg); }
    public static void logE(Exception e) { Log.e(TAG, e.getMessage()); e.printStackTrace(); }
    public static String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

    public static int SENSOR_DELAY = 10000;

    public static String UNCAL_ACC_TITLE = ",utcTimestamp,nanoTimestamp,uncalX,uncalY,uncalZ,biasX,biasY,biasZ,Power,BW2,Step_count,Step_point,PDRg_x,PDRg_y,PDR_Global_Lat,PDR_Global_Lon,Stride,Heading,Measure_Lat,Measure_Lon,In/Out";
    public static String UNCAL_GYRO_TITLE = ",utcTimestamp,nanoTimestamp,uncalX,uncalY,uncalZ,driftX,driftY,driftZ,Power,Rad,Deg";
    public static String UNCAL_MAG_TITLE = ",utcTimestamp,nanoTimestamp,uncalX,uncalY,uncalZ,Roll,Pitch,Yaw(rad),Yaw(deg)";
    public static String ORI_TITLE = ",utcTimestamp,nanoTimestamp,azimuth,pitch,roll";

    public static String WIFI_TITLE = ",utcTimestamp,microTimestamp,bssid,ssid,capabilities,centerFreq0,centerFreq1,channelWidth,frequency,level,operatorFriendlyName,venueName,WiFi_Lat,WiFi_Lon";
    public static String LOA_TITLE = ",utcTimestamp,nanoTimestamp,unixTime,provider,lat,lon,altitude,speed,accuracy,bearing,speedAccuracyMeters,bearingAccuracyDegrees,verticalAccuracyMeters,mock,In/Out";
    public static String GNSS_TITLE = ",utcTimestamp,timeNanos,leapSecond,timeUncertaintyNanos,fullBiasNanos,biasNanos,biasUncertaintyNanos,driftNanosPerSecond,driftUncertaintyNanosPerSecond,hardwareClockDiscontinuityCount,svid,timeOffsetNanos,state,receivedSvTimeNanos,receivedSvTimeUncertaintyNanos,cn0DbHz,pseudorangeRateMetersPerSecond,pseudorangeRateUncertaintyMetersPerSecond,accumulatedDeltaRangeState,accumulatedDeltaRangeMeters,accumulatedDeltaRangeUncertaintyMeters,carrierFrequencyHz,carrierCycles,carrierPhase,carrierPhaseUncertainty,multipathIndicator,snrInDb,constellationType,agcDb,basebandCn0DbHz,fullInterSignalBiasNanos,fullInterSignalBiasUncertaintyNanos,satelliteInterSignalBiasNanos,satelliteInterSignalBiasUncertaintyNanos,codeType,chipsetElapsedRealtimeNanos";


    public final static String[] permission = getPermission();
    private static String[] getPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // 33
            return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        } else {
            return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            };
        }
    }
}
