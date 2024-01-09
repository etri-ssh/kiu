package com.example.sensorlog.scanner.logger;

import android.location.GnssMeasurementsEvent;

public interface GnssLogger { void update(String type, GnssMeasurementsEvent eventArgs); }
