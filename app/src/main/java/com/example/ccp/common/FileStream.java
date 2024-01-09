package com.example.sensorlog.common;

import java.io.File;
import java.io.FileOutputStream;

public class FileStream {
    private String fileName;
    private FileOutputStream fos = null;

    public FileStream(String fileName) { this.fileName = fileName; }

    public void fileCreate(String title) {
        File file = new File(Common.DOWNLOAD_PATH + "/" + fileName + ".csv");
        int count = 0;
        do {
            if(file.exists()) {
                count++;
                fileName = fileName + "(" + count + ")";
                file = new File(Common.DOWNLOAD_PATH + "/" + fileName + ".csv");
                Common.log("File exist count update : " + count);
            } else {
                Common.log("File create name : " + file.getName());
                break;
            }
        } while(true);
        try {
            if(fos == null) {
                fos = new FileOutputStream(file, true);
                fos.write(title.getBytes());
            } else Common.logE(new Exception("Create Error : FileOutputStream is not null"));
        } catch(Exception e) { Common.logE(e); }
    }

    public void fileWrite(String data) {
        try {
            if(fos != null) {
                fos.write(data.getBytes());
            } else Common.logE(new Exception("Write Error : FileOutputStream is null"));
        } catch(Exception e) { Common.logE(e); }
    }

    public void fileClose() {
        try {
            if(fos != null) fos.close();
            else Common.logE(new Exception("Close Error : FileOutputStream is null"));
        } catch(Exception e) { Common.logE(e);
        } finally { fos = null; }
    }
}
