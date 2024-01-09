package com.example.sensorlog.scanner.logger;

import android.hardware.SensorEvent;

public interface SensorLogger { void update(int type, SensorEvent sensorEvent); }
