package com.example.sensorlog.scanner.sensor;

import static com.example.sensorlog.MainActivity.stay_t;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.sensorlog.common.Common;
import com.example.sensorlog.common.FileStream;
import com.example.sensorlog.scanner.base.BaseScanner;

import java.util.ArrayList;
//import com.example.sensorlog.scanner.logger.SensorLogger;


public class UncalMagnetic extends BaseScanner implements SensorEventListener {
    private final SensorManager sensorManager;
    ////////////////////////경일대 추가 부분 시작 (23.09.08)////////////////////////////
    private double Ar,Ap;
    private static double[] mag = new double[3];
    private static double[] magM = new double[3];
    public static double r2d=180.0/Math.PI;
    public static double d2r=Math.PI/180.0;

    private double Roll= 0.0f;                    //Initial Yaw (deg)
    private double Pitch= 0.0f;                    //Initial Yaw (deg)
    private static double bias_M=0.0f;

    private double mMx=0,mMy=0,mMz=0;
    private ArrayList<Double> Magx = new ArrayList<>();
    private ArrayList<Double> Magy = new ArrayList<>();
    private ArrayList<Double> Magz = new ArrayList<>();
    private ArrayList<Double> Mag_H = new ArrayList<>();
    ////////////////////////경일대 추가 부분 종료 (23.09.08)////////////////////////////

    public UncalMagnetic(Context context, String header) {
        super(header);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        fileStream = new FileStream("UncalMagnetic");
    }

    @Override
    public void start() {
        super.start();
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensor, Common.SENSOR_DELAY);
    }

    @Override
    public void stop() {
        super.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        double mx,my,mz,Yaw_m,Yaw_m1;
         double Yaw_c=0.0;

//        timeCount.add(sensorEvent.timestamp); // 1초 당 객체 카운트
        String data = "\n" + (++rowCount) + "";
        data += "," + System.currentTimeMillis();
        data += "," + sensorEvent.timestamp;
        data += "," + sensorEvent.values[0];
        data += "," + sensorEvent.values[1];
        data += "," + sensorEvent.values[2];

        Ar=Mag_Heading.Out_Aroll();
        Ap=Mag_Heading.Out_Apitch();

        ////////////////////////경일대 추가 부분 시작 (23.09.04)////////////////////////////
        mx = sensorEvent.values[1];
        my = sensorEvent.values[0];
        mz = sensorEvent.values[2];

        Mag_Heading.In_mag(mx,my,mz);


        mag=Mag_Heading.Mag_calibration(mx,my,mz);


        bias_M=0;
        Yaw_m=Mag_Heading.Mag_Attitude(mag); //실시간 방위각 계산

        Yaw_m1=Yaw_m*r2d;
        ////////////////////////경일대 추가 부분 시작 (23.09.08)/////////////////////////////
        if (rowCount<stay_t)
        {
            Magx.add(mag[0]);
            Magy.add(mag[1]);
            Magz.add(mag[2]);
        }
        if(rowCount==stay_t){
            magM[0]=Lowpassfilter.mean_D(Magx);
            magM[1]=Lowpassfilter.mean_D(Magy);
            magM[2]=Lowpassfilter.mean_D(Magz);

            Yaw_c=Mag_Heading.Mag_Attitude(magM);
            Heading.Mag_H(Yaw_c);   //지자기 센서를 통한 초기 방위각
        }


        ////////////////////////경일대 추가 부분 종료 (23.09.08)////////////////////////////
        Roll=Mag_Heading.Out_Aroll();
        Pitch=Mag_Heading.Out_Apitch();
        data += "," +Roll;
        data += "," +Pitch;
        data += "," + Yaw_m;            //Mag Heading (Rad)
        data += "," + Yaw_m1;           //Mag Heading (Deg)

        ////////////////////////경일대 추가 부분 종료 (23.09.04)////////////////////////////



        fileStream.fileWrite(data);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
