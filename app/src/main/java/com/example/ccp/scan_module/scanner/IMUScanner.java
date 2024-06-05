package com.example.ccp.scan_module.scanner;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.ccp.common.Common;
import com.example.ccp.scan_module.ScanModule;

import java.util.List;

public class IMUScanner extends BaseScanner{
    /**
     *  sensorManager : Sensor 매니저(registerListener, unregisterListener 호출)
     *  sensorEventListener : Sensor 스캔 결과
     *  scanning : 현재 스캔 되고 있는지 상태( true - 스캔 on || false - 스캔 off )
     */
    private final SensorManager sensorManager;
    private final SensorEventListener sensorEventListener;

    // Activity 출력용
    private String accResult;
    private String gyroResult;
    private String magResult;
    private String oriResult;

    // PDR 알고리즘 처리용
    private float[] accValResult = new float[4];
    private float[] gyroValResult = new float[4];
    private float[] magValResult = new float[4];
    private float[] oriValResult = new float[3];

    // orientation 획득용
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


    public IMUScanner(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            /**
             * @action 센서 스캔 결과 값을 받는 메서드(values 값은 android 문서에서 확인)
             * @param sensorEvent 센서 값
             */
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                int currentType = sensorEvent.sensor.getType();
                if(currentType == Sensor.TYPE_ACCELEROMETER) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    accResult = " x : " + x + ", y : " + y + ", z : " + z;
                    accValResult[0] = System.currentTimeMillis();       // utc
                    accValResult[1] = x;
                    accValResult[2] = y;
                    accValResult[3] = z;


                } else if(currentType == Sensor.TYPE_GYROSCOPE) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    gyroResult = " x : " + x + ", y : " + y + ", z : " + z;
                    gyroValResult[0] = System.currentTimeMillis();
                    gyroValResult[1] = x;
                    gyroValResult[2] = y;
                    gyroValResult[3] = z;

                } else if(currentType == Sensor.TYPE_MAGNETIC_FIELD) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];
                    magResult = " x : " + x + ", y : " + y + ", z : " + z;
                    magValResult[0] = System.currentTimeMillis();
                    magValResult[1] = x;
                    magValResult[2] = y;
                    magValResult[3] = z;

                    // mag가 획득되는 경우에 orientation 계산
                    SensorManager.getRotationMatrix(rotationMatrix, null, accValResult, magValResult);
                    oriValResult = SensorManager.getOrientation(rotationMatrix, orientationAngles);
                    oriResult = " x : " + oriValResult[0] + ", y : " + oriValResult[1] + ", z : " + oriValResult[2];

                }
                ScanModule.acc.postValue(accResult);
                ScanModule.gyro.postValue(gyroResult);
                ScanModule.mag.postValue(magResult);
                ScanModule.ori.postValue(oriResult);

                /** [ETRI] PDR 계산을 위해 문자열, 값을 구분하여 갱신 **/
                ScanModule.accValue.postValue(accValResult);
                ScanModule.gyroValue.postValue(gyroValResult);
                ScanModule.magValue.postValue(magValResult);
                ScanModule.oriValue.postValue(oriValResult);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };
    }

    @Override
    void action() {
        if(loopFlag) {
            loopFlag = false;
            List<Sensor> list =  sensorManager.getSensorList(Sensor.TYPE_ALL);
            for(Sensor item : list) {
                if( item.getType() == Sensor.TYPE_ACCELEROMETER ||
                    item.getType() == Sensor.TYPE_GYROSCOPE ||
                    item.getType() == Sensor.TYPE_MAGNETIC_FIELD
                ){
                    //sensorManager.registerListener(sensorEventListener, item, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(sensorEventListener, item, Common.SENSOR_DELAY); // Delay : 10ms
                }
            }

        }
    }

    @Override
    void close() {
        try {
            sensorManager.unregisterListener(sensorEventListener);
        } catch(Exception e) { Common.logW("[imu] close error"); e.printStackTrace(); }
    }
}
