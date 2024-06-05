package com.example.ccp.scan_module.scanner;

import android.app.Activity;

import com.example.ccp.common.Common;
import com.example.ccp.common.FileStream;
import com.example.ccp.module.pos.FusedModule;

import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseScanner {
    private final Timer timer = new Timer();
    public boolean loopFlag = true;

    public FileStream fileStream;

    public void start(long ms) {
        action();
        timer.schedule(new TimerTask() {
            @Override
            public void run() { action(); }}, 0, ms);
    }

    public void stop() {
        try { close(); timer.cancel(); }
        catch(Exception e) {
            Common.logW("[b.s] start timer error");
            e.printStackTrace();
        }
    }

    abstract void action();
    abstract void close();
}
