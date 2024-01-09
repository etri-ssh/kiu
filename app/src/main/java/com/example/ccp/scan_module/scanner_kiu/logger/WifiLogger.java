package com.example.sensorlog.scanner.logger;

import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiLogger { void update(String type, List<ScanResult> scanList); }
