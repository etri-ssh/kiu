package com.example.ccp.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ccp.databinding.ActivityPdrBinding;
import com.example.ccp.scan_module.ScanModule;
import com.example.ccp.scan_module.pdr.DetectPeak;
import com.example.ccp.scan_module.pdr.Glob_Pos;
import com.example.ccp.scan_module.pdr.Heading;
import com.example.ccp.scan_module.pdr.Mag_Heading;
import com.example.ccp.scan_module.pdr.PDR;
import com.example.ccp.scan_module.pdr.Stride;

import com.example.ccp.scan_module.fingerprinting.DataBase;
import com.example.ccp.scan_module.fingerprinting.Positioning;

import static com.example.ccp.scan_module.pdr.PDR.goTest;
import static com.example.ccp.scan_module.pdr.PDR.time_s;
import static com.example.ccp.scan_module.pdr.PDR.stay_t;
import static com.example.ccp.scan_module.pdr.PDR.PDR_ON_OFF;
import static com.example.ccp.scan_module.fingerprinting.DataBase.dbKey;
import static com.example.ccp.scan_module.pdr.PDR.DNNA;
import static com.example.ccp.module.pos.AndGnssLModule.G_IO;

import com.example.ccp.module.DataCollection;


/***
 코드 수정,추가 사항 2023/8/28
 1.'UncalAccelermeter.java'와 'UncalGyroscope.java'에서 PDR의 Stride와 Heading 정보를 계산하였습니다.
 2.PDR 계산을 위해서 'Lowpassfilter','DetectPeck','Stride','Heading','PDR'총 5개의 클래스를 'com.example.sensorlog>scanner>sensor'에 만들었습니다.
 3.'MainActivity.java'에서 계산된 PDR정보들을 실시간 나타나게 수정 헀습니다.
 4.activity_main에서 PDR정보를 실시간으로 출력하며, '통합 스켄 종료'를 누르게되면 측정된 종합 정보를 표시하게 하였습니다.
 5.MainActivity.java'에서 '통합 스켄 시작'버튼을 누른 후 초기정렬을 위해 60초 대기하기 위해서 화면에 'Stay'와'Go'를 표시하였습니다.

 코드 수정,추가 사항 2023/9/4
 1.2-D PDR 구현을 위한 'PDR.java'class를 생성하였습니다.
 2.Main에서 PDR on/off 버튼을 구현하였습니다. 평평한 곳에 스마트폰을 놓고 Initial Alignment을 위해 30초 대기 후에 PDR On/Off 버튼이 활성화 되며,
 'Pick Up!'이라는 메세지가 표시됩니다. 이때 스마트폰을 들고 'PDR On'버튼을 누르면 걸음 검출과 Stride 게산이 시작됩니다.
 3.'Button_test.java'는 'PDR On'버튼이 눌러졌을 때 걸음 검출을 시작하기 위해서 Class 간의 변수를 이동시키는 class입니다.
 Java 코드에 대해 아직 부족한 부분이 있어서 전역변수 대용으로 사용하였습니다.
 4. 'Mag_Heading.java'는 지자기 센서를 활용하여 Heading을 계산하는 Class입니다. 아직 조정단계이므로 사용할 수 없습니다.
 추후 조정후 설명과 주석을 추가 하겠습니다.
 ***/

/** 코드 수정 사항 2023/9/8

 B_oi -> PDR_ON_OFF 공통 관리

 DeteckPeck Stepout - step_c
 Lowpassfilter out_mA - meanA
 Stride Stride_out - stride

 Heading Heading_Dist - yaw

 Mag_Heading Heading_M_Dist - Yaw_m
 Mag_Heading Out_Aroll - a_roll
 Mag_Heading Out_Apitch - a_pitch

 PDR PDR_Out()- pos
 PDR PDR_M_Out()- posM
 */



///////////////////////////////////////////////////////

public class PDRActivity extends AppCompatActivity {

    private ActivityPdrBinding binding;
    //state1
    //state2
    //state3
    //btn_start
    //btn_end
    //btn_pdr

    int step;
    int step1;
    double stride,stride1, stride2;
    double heading,heading1,headingM;
    boolean isActive = false;

    double r2d=180.0/Math.PI;       //Rad to Deg
    double d2r=Math.PI/180.0;       //Rad to Deg

    private double[] PDR_p = new double[2];     //PDR position (Gyro Heading)
    private double[] PDR_pM = new double[2];    //PDR position (Magnet Heading)


    private PDR pdr = new PDR(this);


    @SuppressLint({"SetTextI18n", "DefaultLocale", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /** [ETRI] 초기위치를 DataCollection에 저장된 FLP 위치를 가져오도록 설정 (24.01.30) **/
        Glob_Pos.Get_GPS(DataCollection.instance.fusedLast().lat * d2r, DataCollection.instance.fusedLast().lon * d2r);
        //Common.log("lat: " + DataCollection.instance.fusedLast().lat * d2r + ", lon: " + DataCollection.instance.fusedLast().lon * d2r );// test

        step_c();

        binding.btnStart.setOnClickListener(view -> {
            binding.state1.setText("Step : Stay...");

            /** [ETRI] imu 데이터가 PDRactivity에서도 잘 갱신되는지 보기 위해 추가
             * 현재의 PDR은 PDRActivitiy 화면을 띄우고 있는 와중에만 동작하도록 우선 되어 있는듯
             * 앱 전체에서 동작하도록 추후 수정이 필요함
             * **/
            // Android 센서값 출력
            ScanModule.acc.observe(this, str->{binding.acc.setText(str);});
            ScanModule.gyro.observe(this,str->{binding.gyro.setText(str);});
            ScanModule.mag.observe(this,str->{binding.mag.setText(str);});
            ScanModule.ori.observe(this,str->{binding.ori.setText(str);});

            // 버튼이 클릭된 순간부터 pdr 알고리즘 시작
            ScanModule.accValue.observe(this, accVal -> {
                pdr.PdrAccOperation(accVal);
            });

            ScanModule.gyroValue.observe(this, gyroVal -> {
                pdr.pdrGyroOperation(gyroVal);
            });

            ScanModule.magValue.observe(this, magVal -> {
                pdr.pdrMagOperation(magVal);
            });


            isActive = true;
            step_c();       // 삭제 혹은 변경 필요?

            new Handler().postDelayed(() -> {
                binding.btnPdr.setEnabled(true);
                binding.state1.setText("Step : Pick up!");

                time_s=1;   //ki
            },stay_t*10);           // stay_t is 10ms period counter. so stay_t*10 --> 3000*10 ms
        });
        binding.btnEnd.setOnClickListener(view->{
            binding.btnPdr.setEnabled(false);       // PDR button deactivation
            PDR_ON_OFF=0;
            isActive = false;
            step = DetectPeak.step_c;    //Step Count
            stride = Stride.Stride_Dist();  //Stride Add data
            heading = Heading.yaw;          //Heading data
            Stride.Stride_reset();          //Stride Data Reset

            binding.state1.setText("Step : "+step+"\r\nStride : "+String.format("%.2f",stride)+"(m)");

            binding.state2.setText("Step : "+step+"\r\nStride : "+String.format("%.2f",stride)+"(m)" +
                    "\r\nHeading (Gyro)   : "+String.format("%.2f",heading1)+"(deg)" +
                    "\r\nHeading (Magnet) : "+String.format("%.2f",headingM)+"(deg)"
                    +PDR_ON_OFF
            );
            DetectPeak.Stepreset();
            Stride.Stride_reset();
        });
        binding.btnPdr.setOnClickListener(view -> {
            PDR_ON_OFF=1;
            goTest = 1;
            binding.state1.setText("Step : Go!");
        });
        binding.btnDb.setOnClickListener(view -> {
            if(dbKey){
                Toast.makeText(getApplicationContext(),"DB 업로드를 진행합니다",Toast.LENGTH_SHORT).show();
                DataBaseParsing();
                Toast.makeText(getApplicationContext(), "DB 업로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                dbKey = false;
            }else Toast.makeText(getApplicationContext(), "이미 DB가 업로드된 상태입니다.", Toast.LENGTH_SHORT).show();
        });
    }


    // PDR activity 에서만 IMU 및 PDR 로그 결과가 저장되도록 설정
    // (이후 MainActivity에 PDR을 출력할 경우에 filestream 관리 방법을 변경하면서 변경이 필요함
    @Override
    protected void onDestroy() {        
        pdr.close();
        super.onDestroy();
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void step_c(){
        step1 = DetectPeak.step_c;
        stride1 = Stride.stride;
        heading1=Heading.yaw;
        heading1=heading1*r2d;
        PDR_p = PDR.pos;

        if (DNNA ==0){
            stride2=0;
        }else{
            stride2=stride1;
        }


        headingM= Mag_Heading.Yaw_m;
        headingM=headingM*r2d;

        binding.state2.setText("Step : "+step1+"\r\nStride : "+String.format("%.2f",stride1)+"(m) / "+String.format("%.2f",stride2)+"(m)  DNN : " +DNNA +//Stride(m)
                "\r\nHeading (Gyro)   : "+String.format("%.2f",heading1)+"(deg)" +           //Gyro Heading (deg)
                "\r\nHeading (Magnet) : "+String.format("%.2f",headingM)+"(deg)"+            //Magnet Heading (deg)
                "\r\nIn/Out : "+ G_IO);  //PDR data : Stride, Heading(Gyro, Magnet) //In/Out 확인

        // 경일대 WiFi 정보 추가
        binding.state3.setText("PDR_G x : "+String.format("%.2f",PDR_p[0])+", y : " +String.format("%.2f",PDR_p[1])+            //PDR Local 좌표계
                "\r\nWiFi_FingerPrinting \r\n Lon_W : "+ Positioning.Pos_F[0] +"\r\n Lat : " +Positioning.Pos_F[1]+", \r\n Floor : " +Positioning.Pos_F[2]+"\n\r Lon_G : "+ Glob_Pos.GPS_P[0]+"\n\rLat_G : "+ Glob_Pos.GPS_P[1]);  // Fingerprint 위치 정보


/*      // ETRI-heliosen wifi finger printing 정보 출력
        binding.state3.setText(
                "PDR_G x : "+String.format("%.2f",PDR_p[0])+
                        ", y : " +String.format("%.2f",PDR_p[1])+       //pdr local
                        "\r\nWiFi_FingerPrinting \r\n Lon : "+ FingerPrinting.Pos_F[0] +
                        ", \r\n Lat : " +FingerPrinting.Pos_F[1]+
                        ", \r\n Floor : " +FingerPrinting.Pos_F[2]);
*/

        if(isActive){ refresh(10); } //0.01s delay update
    }

    private void refresh(int ms){
        final Handler handler = new Handler();
        final Runnable runnable = () -> step_c();
        handler.postDelayed(runnable,ms);
    }

    /*private void toast(String msg) { runOnUiThread(() ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()); }
*/
    
    // 경일대 Wi-Fi 결과를 가져오는 것(아래결과와 switching 할수 있게 만들어야함)
    private void DataBaseParsing() {
        DataBase.initArray();
        DataBase.jsonParsing(this);
    }

    // setFDB는 heliosen Wifi 결과를 가져오는것 (일단 유지할 필요가 있음)
//    private void DataBaseParsing() {
//        FingerPrinting.setFDB(this);
//    }

}