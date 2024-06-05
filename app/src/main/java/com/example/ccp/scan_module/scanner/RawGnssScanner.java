package com.example.ccp.scan_module.scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.LocationManager;
import android.os.Build;

import com.example.ccp.common.Common;

import java.util.Collection;

import com.example.ccp.common.FileStream;
import com.example.ccp.scan_module.ScanModule;


/**
 * @ETRI GNSS raw measurement scanner
 * 주기적으로 GNSS raw measurement를 획득하고 저장
 * GNSSModule에서 rawmeasurement를 불러오는 부분이 있지만,
 * GnssModule의 개별 독장보장을 위해 별도 스캐너를 추가
 */

@SuppressLint("MissingPermission")
public class RawGnssScanner extends BaseScanner {

    private final LocationManager locationManager;

    private int rowCount = 0;

    public RawGnssScanner(Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        fileStream = new FileStream("rawGnss");
        fileStream.fileCreate(Common.GNSS_RAW_TITLE); // 파일 로그 기능은 우선 삭제
    }

    @Override
    void action() {

        if(loopFlag){
            loopFlag = false;
            locationManager.registerGnssMeasurementsCallback(callback);
        } else {

        }
    }

    @Override
    void close() {
        locationManager.unregisterGnssMeasurementsCallback(callback);
        fileStream.fileClose();
    }


    private final GnssMeasurementsEvent.Callback callback = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {

            super.onGnssMeasurementsReceived(eventArgs);

            StringBuilder sb = new StringBuilder();

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

                sb.append("[SVID:" + gm.getSvid()+",[CN0]:"+gm.getCn0DbHz() + "\n" );

            }

            ScanModule.rawGnss.postValue(""+sb);

        }
    };


}
