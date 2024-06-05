package com.example.ccp.scan_module.scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Build;

import com.example.ccp.common.Common;

import java.util.Collection;

import com.example.ccp.common.FileStream;
import com.example.ccp.scan_module.ScanModule;

/**
 * @ETRI GNSS raw measurement status
 */

@SuppressLint("MissingPermission")
public class GnssStatusScanner extends BaseScanner {

    private final LocationManager locationManager;

    private int rowCount = 0;

    public GnssStatusScanner(Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    void action() {

        if(loopFlag){
            loopFlag = false;
            locationManager.registerGnssStatusCallback(callback);
        } else {

        }
    }


    @Override
    void close() {
        locationManager.unregisterGnssStatusCallback(callback);
    }

    private final GnssStatus.Callback callback = new GnssStatus.Callback() {

        StringBuilder sb = new StringBuilder();

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

                sb.append("[SVID:" + status.getSvid(i)+",[CN0]:"+status.getCn0DbHz(i) + "\n" );
            }

            ScanModule.rawGnss.postValue(""+sb);

        }
    };


}
