package com.example.sensorlog.scanner.logger;

import android.location.GnssStatus;

public interface StatusLogger { void update(String type, GnssStatus status); }
