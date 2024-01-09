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

public class UncalGyroscope extends BaseScanner implements SensorEventListener {
    private final SensorManager sensorManager;

    ////////////////////////경일대 추가 부분 시작 (23.09.04)////////////////////////////
    public static double[] gyro= new double[3];
    public static double[] Wgyro= new double[3];
    public static double[] mAccel= new double[3];
    public  double yaw_C=0;
    public static double[] Pos_s= new double[3];
    public static double[] mGyro= new double[3];
    private ArrayList<Double> Gyrox = new ArrayList<>();
    private ArrayList<Double> Gyroy = new ArrayList<>();
    private ArrayList<Double> Gyroz = new ArrayList<>();
    private double Yaw=0.0f;
    private double Yaw2= 0.0f;                    //Initial Yaw (deg)

    private double d_time1= 0.0f;
    private double time_e1= 0.0f;
    private double dt1= 0.0f;
    private int a= 1;


    ////////////////////////경일대 추가 부분 종료 (23.09.04)////////////////////////////
    public UncalGyroscope(Context context, String header) {
        super(header);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        fileStream = new FileStream("UncalGyroscope");
    }

    @Override
    public void start() {
        super.start();
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
            sensorManager.registerListener(this, sensor, Common.SENSOR_DELAY);
    }

    @Override
    public void stop() {
        super.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        double gx,gy,gz,Gpow;

        String data = "\n" + (++rowCount) + "";
        data += "," + System.currentTimeMillis();
        data += "," + sensorEvent.timestamp;
        data += "," + sensorEvent.values[0];
        data += "," + sensorEvent.values[1];
        data += "," + sensorEvent.values[2];
        data += "," + sensorEvent.values[3];
        data += "," + sensorEvent.values[4];
        data += "," + sensorEvent.values[5];

        gx=sensorEvent.values[0];
        gy=sensorEvent.values[1];
        gz=sensorEvent.values[2];

        ////////////////////////경일대 추가 부분 시작 (23.09.04)////////////////////////////

        // 센서 데이터 사이의 정확한 시간 생성
        time_e1=sensorEvent.timestamp-d_time1;

        d_time1=sensorEvent.timestamp;
        dt1=time_e1/1000000000;            //nano time=>sec   1*10^-9

        gyro[0]=sensorEvent.values[1];
        gyro[1]=sensorEvent.values[0];
        gyro[2]=-sensorEvent.values[2];


        // Power 값 생성
        Gpow=Math.pow(gx,2) +Math.pow(gy,2) +Math.pow(gz,2);

        yaw_C=Heading.yawC;  //지자기 방위각
        data += "," + Gpow;
        if (rowCount<stay_t) {
            Gyrox.add(gyro[0]);
            Gyroy.add(gyro[1]);
            Gyroz.add(gyro[2]);
            data += ",,";  // Heading (deg)
        }else if(rowCount>=stay_t && yaw_C!=0 && a==1) {
            mGyro[0]=Lowpassfilter.mean_D(Gyrox);
            mGyro[1]=Lowpassfilter.mean_D(Gyroy);
            mGyro[2]=Lowpassfilter.mean_D(Gyroz);

            mAccel = Lowpassfilter.meanA; //Lowpassfilter.out_mA();
        //    yaw_C=-95*(Math.PI/180.0); //직접 초기 방위각 설정
            Heading.Init_Align( mAccel, Pos_s, mGyro,gyro,yaw_C);
            data += ",,";
            a=0;
        }else {
            Yaw =Heading.Attitude_Update(gyro,dt1);
            data += "," +Yaw;   // Heading (rad)
            data += "," +Yaw2;  // Heading (deg)
        }
        Yaw2=Yaw*180.0/Math.PI;


        ////////////////////////경일대 추가 부분 종료 (23.09.04)////////////////////////////

        fileStream.fileWrite(data);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
