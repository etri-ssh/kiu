package com.example.ccp.views;

import static com.example.ccp.common.MyApplication.shared;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ccp.common.BottomToast;
import com.example.ccp.common.Common;
import com.example.ccp.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvReset.setOnClickListener(v-> defaultData()); // 기본값 설정
        binding.tvSave.setOnClickListener(v-> save()); // 저장

        binding.scanInfoLayout.setOnClickListener(v->{ // 스캔 정보 확인 으로 이동
            Intent intent = new Intent(getApplicationContext(),ScanActivity.class);
            startActivity(intent);
        });
        binding.pdrCalcLayout.setOnClickListener(v->{ // PDR 측정 으로 이동
            Intent intent = new Intent(getApplicationContext(),PDRActivity.class);
            startActivity(intent);
        });

        initData();
    }

    // 설정 값 가져 오기
    private void initData() {
        // 실내 지도
        binding.tvMapInfo.setText(shared.getUrlMap()); // 서버 정보
        binding.schAutoMap.setChecked(MainActivity.autoFlag); // 자동 지도
        binding.spMapRadius.setSelection(shared.getMapRadius()); // 반경

        // gnss
        binding.schGnss.setChecked(shared.getGnssState()); // on,off
        binding.spGnssType.setSelection(shared.getGnssMount()); // 보정 지역

        // signal
        binding.tvSignalInfo.setText(shared.getUrlSignal()); // 서버 정보
        binding.schSignal.setChecked(shared.getSignalState()); // on,off
        binding.edtSignalTimer.setText(String.valueOf(shared.getSignalTimer())); // signal 측위 주기
        binding.edtBleTimer.setText(String.valueOf(shared.getBleTimer())); // ble 주기
        binding.edtLteTimer.setText(String.valueOf(shared.getLteTimer())); // ble 주기
        binding.edtWifiTimer.setText(String.valueOf(shared.getWifiTimer())); // ble 주기
        binding.edtImuTimer.setText(String.valueOf(shared.getImuTimer())); // ble 주기

        // image
        binding.tvImageInfo.setText(shared.getUrlImage()); // 서버 정보
        binding.schImage.setChecked(shared.getImageState()); // on,off

        // 측위 연계
        binding.edtCcpTimer.setText(String.valueOf(shared.getFinalTimer())); // 측위 주기
    }

    // 기본 값으로 모두 변경(자동 지도 제외)
    private void defaultData() {
        // 실내 지도
        binding.tvMapInfo.setText(Common.DEFAULT_MAP_URL); // 서버 정보
        binding.spMapRadius.setSelection(0); // 반경

        // gnss
        binding.schGnss.setChecked(true); // on,off
        binding.spGnssType.setSelection(0); // 보정 지역

        // signal
        binding.tvSignalInfo.setText(Common.DEFAULT_SIGNAL_URL); // 서버 정보
        binding.schSignal.setChecked(true); // on,off
        binding.edtSignalTimer.setText(String.valueOf(Common.SIGNAL)); // signal 측위 주기
        binding.edtBleTimer.setText(String.valueOf(Common.BLE)); // ble 주기
        binding.edtLteTimer.setText(String.valueOf(Common.LTE)); // lte 주기
        binding.edtWifiTimer.setText(String.valueOf(Common.WIFI)); // wifi 주기
        binding.edtImuTimer.setText(String.valueOf(Common.IMU)); // imu 주기

        // image
        binding.tvImageInfo.setText(Common.DEFAULT_IMAGE_URL); // 서버 정보
        binding.schImage.setChecked(true); // on,off

        // 측위 연계
        binding.edtCcpTimer.setText(String.valueOf(Common.FINAL)); // 측위 주기
    }

    // 설정 값 모두 저장
    private void save() {
        if(isEmpty(binding.edtSignalTimer)) {
            BottomToast.createToast(this, "신호 측위 주기를 설정해 주세요").show();
        } else if(isEmpty(binding.tvMapInfo)) {
            BottomToast.createToast(this, "실내 지도 서버 정보를 입력해 주세요").show();
        } else if(isEmpty(binding.tvSignalInfo)) {
            BottomToast.createToast(this, "신호 기반 측위 서버 정보를 입력해 주세요").show();
        } else if(isEmpty(binding.tvImageInfo)) {
            BottomToast.createToast(this, "영상 기반 측위 서버 정보를 입력해 주세요").show();
        } else if(isEmpty(binding.edtBleTimer)) {
            BottomToast.createToast(this, "BLE 주기를 설정해 주세요").show();
        } else if(isEmpty(binding.edtLteTimer)) {
            BottomToast.createToast(this, "LTE 주기를 설정해 주세요").show();
        } else if(isEmpty(binding.edtWifiTimer)) {
            BottomToast.createToast(this, "WIFI 주기를 설정해 주세요").show();
        } else if(isEmpty(binding.edtImuTimer)) {
            BottomToast.createToast(this, "IMU 주기를 설정해 주세요").show();
        } else if(isEmpty(binding.edtCcpTimer)) {
            BottomToast.createToast(this, "측위 연계 주기를 설정해 주세요").show();
        } else {
            shared.setUrlMap(binding.tvMapInfo.getText().toString()); // 실내 지도 서버 정보
            shared.setUrlSignal(binding.tvSignalInfo.getText().toString()); // 신호 기반 측위 서버 정보
            shared.setUrlImage(binding.tvImageInfo.getText().toString()); // 영상 기반 측위 서버 정보
            shared.setMapRadius(binding.spMapRadius.getSelectedItemPosition()); // 반경
            shared.setGnssState(binding.schGnss.isChecked()); // gnss on,off
            shared.setGnssMount(binding.spGnssType.getSelectedItemPosition()); // 보정 지역
            shared.setSignalState(binding.schSignal.isChecked()); // signal on,off
            shared.setSignalTimer(Long.parseLong(binding.edtSignalTimer.getText() + "")); // 신호 주기
            shared.setBleTimer(Long.parseLong(binding.edtBleTimer.getText() + "")); // ble 주기
            shared.setLteTimer(Long.parseLong(binding.edtLteTimer.getText() + "")); // lte 주기
            shared.setWifiTimer(Long.parseLong(binding.edtWifiTimer.getText() + "")); // wifi 주기
            shared.setImuTimer(Long.parseLong(binding.edtImuTimer.getText() + "")); // imu 주기
            shared.setImageState(binding.schImage.isChecked()); // 영상 on,off
            shared.setFinalTimer(Long.parseLong(binding.edtCcpTimer.getText() + "")); // 연계 주기
            BottomToast.createToast(this,"설정 값을 저장하기위해 앱이 종료됩니다.").show();
            Context mContext = this;
            new Handler().postDelayed(() -> ActivityCompat.finishAffinity((Activity) mContext),2000);
        }
    }

    private boolean isEmpty(EditText target) { return target.getText() == null || target.getText().length() == 0; }
}