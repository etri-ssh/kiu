package com.example.sensorlog.scanner;

import android.widget.Switch;

import com.example.sensorlog.scanner.base.BaseScanner;
import com.example.sensorlog.scanner.enums.State;

import java.util.ArrayList;
import java.util.List;

public class ScannerController {
    private BaseScanner[] scanners;
    private boolean running = false;

    public void initScanner(BaseScanner... scanners) {this.scanners = scanners;}

    public synchronized State start() {
        if(!running) {
            if(scanners.length < 1) return State.EMPTY;
            else {
                running = true;
                for(BaseScanner bs : scanners) bs.start();
            }
        } else return State.IS_RUNNING;
        return State.SUCCESS;
    }
    public synchronized boolean stop() {
        if(running) {
            running = false;
            for(BaseScanner bs : scanners) bs.stop();;
            return true;
        } else return false;
    }
}
