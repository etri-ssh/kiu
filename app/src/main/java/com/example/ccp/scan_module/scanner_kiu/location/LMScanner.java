package com.example.sensorlog.scanner.location;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.logger.LocationLogger;

@SuppressLint({"MissingPermission", "InlinedApi"})
public class LMScanner extends BaseScanner {
    private final GpsLScanner gpsLScanner;
    private final NlpLScanner nlpLScanner;
    private final FlpLScanner flpLScanner;

    public LMScanner(Context context, String header, LocationLogger locationLogger) {
        super(header);
        gpsLScanner = new GpsLScanner(context, header, locationLogger);
        nlpLScanner = new NlpLScanner(context, header, locationLogger);
        flpLScanner = new FlpLScanner(context, header, locationLogger);
    }

    @Override
    public void start() {
        gpsLScanner.start();
        nlpLScanner.start();
        flpLScanner.start();
    }

    @Override
    public void stop() {
        gpsLScanner.stop();
        nlpLScanner.stop();
        flpLScanner.stop();
    }
}
