/** [ETRI]
 * 기존 WIFIScanner에 함께 있던 코드를
 * 별도 모듈로 작성해서 이전하기 위한 모듈
 * 
 * 이를 위한 모듈 형태만 우선 구성
 * (24.01.22) **/


package com.example.ccp.module.pos;

import static com.example.ccp.common.MyApplication.shared;

import androidx.annotation.NonNull;

import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.API;
import com.example.ccp.common.retrofit.RetrofitClient;
import com.example.ccp.common.retrofit.model.PayloadRequest;
import com.example.ccp.common.retrofit.model.SessionLogRequest;
import com.example.ccp.common.retrofit.model.SignalRequest;
import com.example.ccp.common.retrofit.model.SignalResponse;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.SignalPositioning;
import com.example.ccp.scan_module.scanner.WIFIScanner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** @ETRI 6월초 이후 Wi-Fi 데이터 활용 방법 변경 예정 **/

public class KiWifiModule {

    // signal 모듈 getlastrequest 활용하는 방법으로 작성 필요
}
