package com.example.ccp.views;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ccp.R;
import com.example.ccp.databinding.ActivityScanBinding;
import com.example.ccp.scan_module.ScanModule;


public class ScanActivity extends AppCompatActivity {

    private ActivityScanBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivClose.setOnClickListener(view -> finish());

        ScanModule.acc.observe(this, str->{binding.acc.setText(str);});
        ScanModule.gyro.observe(this,str->{binding.gyro.setText(str);});
        ScanModule.mag.observe(this,str->{binding.mag.setText(str);});
        ScanModule.ble.observe(this,str->{binding.ble.setText(str);});
        ScanModule.wifi.observe(this,str->{binding.wifi.setText(str);});
        ScanModule.lte.observe(this,str->{binding.lte.setText(str);});

        ScanModule.rawGnss.observe(this,str->{binding.rawGnss.setText(str);});

    }
}