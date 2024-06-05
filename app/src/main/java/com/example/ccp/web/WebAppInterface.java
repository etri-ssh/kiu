package com.example.ccp.web;

import android.webkit.JavascriptInterface;

public interface WebAppInterface {
    @JavascriptInterface
    void cameraAction(int visibility);

    @JavascriptInterface
    void locationAction(boolean flag);

    @JavascriptInterface
    void floorChangeAction(double floor);
    @JavascriptInterface
    String currentPosition();

}
