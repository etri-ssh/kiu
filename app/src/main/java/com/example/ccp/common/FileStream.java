package com.example.ccp.common;

import java.io.File;
import java.io.FileOutputStream;

public class FileStream {
    private String fileName;
    private FileOutputStream fos = null;
    public FileStream(String fileName) { this.fileName = fileName; }

    public void fileCreate(String title) {
        File file = new File(Common.PUBLIC_DOWNLOAD + "/" + fileName + ".csv");
        int count = 0;
        do {
            if(file.exists()) {
                count++;
                fileName = fileName + "(" + count + ")";
                file = new File(Common.PUBLIC_DOWNLOAD + "/" + fileName + ".csv");
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
            } else Common.logW("Create Error : FileOutputStream is not null");
        } catch(Exception e) { Common.logW("File create error : " + e); e.printStackTrace(); }
    }

    public void fileWrite(String data) {
        try {
            if(fos != null) {
                fos.write(data.getBytes());
            } else Common.logW("Write Error : FileOutputStream is null");
        } catch(Exception e) { Common.logW("File write error : " + e); e.printStackTrace(); }
    }

    public void fileClose() {
        try {
            if(fos != null) fos.close();
            else Common.logW("Close Error : FileOutputStream is null");
        } catch(Exception e) { Common.logW("File close error : " + e); e.printStackTrace();
        } finally { fos = null; }
    }
}
