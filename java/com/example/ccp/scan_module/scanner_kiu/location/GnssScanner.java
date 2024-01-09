package com.example.sensorlog.scanner.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.os.Build;

import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.logger.GnssLogger;

import java.util.Collection;

@SuppressLint("MissingPermission")
public class GnssScanner extends BaseScanner {
    private final LocationManager locationManager;
    private final GnssLogger gnssLogger;

    public GnssScanner(Context context, String header,GnssLogger gnssLogger) {
        super(header);
        this.gnssLogger = gnssLogger;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        fileStream = new FileStream("Gnss");
    }

    @Override
    public void start() {
     //   if(!activation) {
            super.start();
            locationManager.registerGnssMeasurementsCallback(callback);
     //   }
    }

    @Override
    public void stop() {
        super.stop();
        locationManager.unregisterGnssMeasurementsCallback(callback);
    }

    private final GnssMeasurementsEvent.Callback callback = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
            GnssClock gnssClock = eventArgs.getClock();
            Collection<GnssMeasurement> gnssMeasurements = eventArgs.getMeasurements();
            for(GnssMeasurement gm : gnssMeasurements) {
                String data = "\n" + (++rowCount) + "";
                data += "," + System.currentTimeMillis();
                data += "," + gnssClock.getTimeNanos();
                data += "," + gnssClock.getLeapSecond();
                data += "," + gnssClock.getTimeUncertaintyNanos();
                data += "," + gnssClock.getFullBiasNanos();
                data += "," + gnssClock.getBiasNanos();
                data += "," + gnssClock.getBiasUncertaintyNanos();
                data += "," + gnssClock.getDriftNanosPerSecond();
                data += "," + gnssClock.getDriftUncertaintyNanosPerSecond();
                data += "," + gnssClock.getHardwareClockDiscontinuityCount();
                data += "," + gm.getSvid();
                data += "," + gm.getTimeOffsetNanos();
                data += "," + gm.getState();
                data += "," + gm.getReceivedSvTimeNanos();
                data += "," + gm.getReceivedSvTimeUncertaintyNanos();
                data += "," + gm.getCn0DbHz();
                data += "," + gm.getPseudorangeRateMetersPerSecond();
                data += "," + gm.getPseudorangeRateUncertaintyMetersPerSecond();
                data += "," + gm.getAccumulatedDeltaRangeState();
                data += "," + gm.getAccumulatedDeltaRangeMeters();
                data += "," + gm.getAccumulatedDeltaRangeUncertaintyMeters();
                data += "," + gm.getCarrierFrequencyHz();
                data += ","; // + gm.getCarrierCycles(); 28부터 Deprecated 대체 : getAccumulatedDeltaRangeMeters()
                data += ","; // + gm.getCarrierPhase(); 28부터 Deprecated 대체 : getAccumulatedDeltaRangeMeters()
                data += ","; // + gm.getCarrierPhaseUncertainty(); 28부터 Deprecated 대체 : getAccumulatedDeltaRangeUncertaintyMeters()
                data += "," + gm.getMultipathIndicator();
                data += "," + gm.getSnrInDb();
                data += "," + gm.getConstellationType();
                data += ","; // AgcDb 확인 안됨
                data += ","; if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += gm.getBasebandCn0DbHz();
                data += ","; if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += gm.getFullInterSignalBiasNanos();
                data += ","; if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += gm.getFullInterSignalBiasUncertaintyNanos();
                data += ","; if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += gm.getSatelliteInterSignalBiasNanos();
                data += ","; if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) data += gm.getSatelliteInterSignalBiasUncertaintyNanos();
                data += "," + gm.getCodeType();
                data += ","; // ChipsetElapsedRealtimeNanos 확인 안됨
                fileStream.fileWrite(data);
            }
        //    if(activation) gnssLogger.update("gnss", eventArgs);
        }
    };
}
