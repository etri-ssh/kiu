package com.example.sensorlog.scanner.base;

import com.example.sensorlog.common.FileStream;

public class BaseScanner  {
    public final String header;
//    public boolean activation = false;
    public FileStream fileStream;
    public int rowCount = 0;

    public BaseScanner(String header) { this.header = header; }

    public void start() {
//        activation = true;
        fileStream.fileCreate(header);
    }

    public void stop() {
//        if(activation) {
//            activation = false;
//            fileStream.fileClose();
//        }
        fileStream.fileClose();
    }
}
