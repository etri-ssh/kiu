package com.example.ccp.common;

import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionCheck {
    private Activity activity;
    private Context context;
    private List<Object> resultList;
    private int requestCode = 1000;
    private String[] permissions;

    public PermissionCheck(Activity activity, Context context, String[] permissions) {
        this.activity = activity;
        this.context = context;
        this.permissions = permissions;
    }

//    public boolean start() {
//        if (!this.checkPermission()) {
//            this.requestPermission();
//        }
//    }

    public boolean checkPermission() {
        this.resultList = new ArrayList();
        String[] var2 = this.permissions;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String p = var2[var4];
            int result = ContextCompat.checkSelfPermission(this.context, p);
            if (result != 0) {
                this.resultList.add(p);
            }
        }

        return this.resultList.isEmpty();
    }

    public void requestPermission() {
//        Log.d("dotton","size : "+this.resultList.size());

        ActivityCompat.requestPermissions(this.activity, (String[])this.resultList.toArray(new String[this.resultList.size()]), this.requestCode);
    }
}
