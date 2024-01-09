package com.example.sensorlog.scanner.sensor;

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

public class Orientation extends BaseScanner implements SensorEventListener {
    private final SensorManager sensorManager;
//    private final SensorLogger sensorLogger;
    private final Sensor accSensor;
    private final Sensor magSensor;
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final float[] accValue = new float[3];
    private final float[] magValue = new float[3];

    public Orientation(Context context, String header) {
        super(header);
//        this.sensorLogger = sensorLogger;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        fileStream = new FileStream("Orientation");
    }

    @Override
    public void start() {
//        if(!activation) {
            super.start();
            sensorManager.registerListener(this, accSensor, Common.SENSOR_DELAY);
            sensorManager.registerListener(this, magSensor, Common.SENSOR_DELAY);
//        }
    }

    @Override
    public void stop() {
        super.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double yaw;
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accValue[0] = sensorEvent.values[0];
            accValue[1] = sensorEvent.values[1];
            accValue[2] = sensorEvent.values[2];
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magValue[0] = sensorEvent.values[0];
            magValue[1] = sensorEvent.values[1];
            magValue[2] = sensorEvent.values[2];
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accValue, magValue);
        float[] oriValue = SensorManager.getOrientation(rotationMatrix, orientationAngles);
        String data = "\n" + (++rowCount) + "";
        data += "," + System.currentTimeMillis();
        data += "," + sensorEvent.timestamp;
        data += "," + oriValue[0];
        data += "," + oriValue[1];
        data += "," + oriValue[2];
        fileStream.fileWrite(data);

//        if(activation) sensorLogger.update(3, sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
