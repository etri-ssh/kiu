package com.example.sensorlog;

import static com.example.sensorlog.common.Common.ORI_TITLE;
import static com.example.sensorlog.common.Common.UNCAL_ACC_TITLE;
import static com.example.sensorlog.common.Common.UNCAL_GYRO_TITLE;
import static com.example.sensorlog.common.Common.UNCAL_MAG_TITLE;
import static com.example.sensorlog.common.Common.WIFI_TITLE;
import static com.example.sensorlog.common.Common.GNSS_TITLE;
import static com.example.sensorlog.common.Common.LOA_TITLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.example.sensorlog.Fingerprinting.DataBase;
import com.example.sensorlog.Fingerprinting.Positioning;
import com.example.sensorlog.common.Common;
import com.example.sensorlog.databinding.ActivityMainBinding;
import com.example.sensorlog.scanner.location.GnssScanner;
import com.example.sensorlog.scanner.location.GpsLScanner;
import com.example.sensorlog.scanner.location.LMScanner;
import com.example.sensorlog.scanner.sensor.DetectPeck;
import com.example.sensorlog.scanner.sensor.Glob_Pos;
import com.example.sensorlog.scanner.sensor.Heading;
import com.example.sensorlog.scanner.sensor.Mag_Heading;
import com.example.sensorlog.scanner.sensor.Orientation;
import com.example.sensorlog.scanner.ScannerController;
import com.example.sensorlog.scanner.sensor.PDR;
import com.example.sensorlog.scanner.sensor.Stride;
import com.example.sensorlog.scanner.sensor.UncalAccelerometer;
import com.example.sensorlog.scanner.sensor.UncalGyroscope;
import com.example.sensorlog.scanner.sensor.UncalMagnetic;
import com.example.sensorlog.scanner.signal.WifiScanner;

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
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ScannerController scannerController;
    ////////////////////////경일대 추가 부분 시작 (23.09.04)////////////////////////////

    int step;
    int step1;
    double stride,stride1;
    double heading,heading1,headingM;

    boolean isActive=false;
    public static int stay_t=6000;                            //Delay Time -> 30000 = 30(sec)
    double r2d=180.0/Math.PI;                           //Rad to Deg
    double d2r=Math.PI/180.0;                           //Rad to Deg
    private static double[] PDR_p = new double[2];      //PDR position (Gyro Heading)

    public static int PDR_ON_OFF; //PDR BUTTON  1 : ON , 0 : OFF
    public static int time_s; //PDR BUTTON  1 : ON , 0 : OFF
    public static boolean dbKey = true;   //DB on/off

    ////////////////////////경일대 추가 부분 종료 (23.09.04)////////////////////////////
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


 ////////////////////////경일대 수정 부분 시작 (23.09.04)////////////////////////////
        checkPermission();
        initScannerController();

        step_c();

        binding.bStart.setOnClickListener(view -> {
            binding.AccLPF.setText("Step : Stay...");
            isActive=true;
            step_c();
            switch(scannerController.start()) {
                case SUCCESS: toast("통합 스캔을 실행 합니다"); break;
                case IS_RUNNING: toast("이미 실행 되고 있습니다"); break;
                case EMPTY: toast("실행 할 Scanner가 없습니다"); break;
            }

            new Handler().postDelayed(new Runnable(){
            @Override
                public void run(){
                binding.bPDR.setEnabled(true);         // PDR On/Off 버튼 활성화
                binding.AccLPF.setText("Step : Pick up!");
                time_s=1;
                }
            }, stay_t*10);	//60초

        });
        binding.bStop.setOnClickListener(view -> {
            binding.bPDR.setEnabled(false);            // PDR On/Off 버튼 비활성화
            PDR_ON_OFF = 0;
            isActive=false;
            step= DetectPeck.step_c;         // Step Count   Before : DetectPeck.Stepout()
            stride= Stride.Stride_Dist();    //Stride Add data
            heading=Heading.yaw;             // Heading data   Before : Heading.Heading_Dist()
            Stride.Stride_reset();           //Stride Data Reset

            binding.AccLPF.setText("Step : "+step+"\r\nStride : "+String.format("%.2f",stride)+"(m)");
            binding.AccLPF2.setText("Step : "+step+"\r\nStride : "+String.format("%.2f",stride)+"(m)" +
                    "\r\nHeading (Gyro)   : "+String.format("%.2f",heading1)+"(deg)" +
                    "\r\nHeading (Magnet) : "+String.format("%.2f",headingM)+"(deg)"
                    +PDR_ON_OFF
            );
            DetectPeck.Stepreset();
            Stride.Stride_reset();

            if(scannerController.stop()) toast("통합 스캔을 종료 합니다");
            else toast("스캔이 실행 되고 있지 않습니다");
        });

         //PDR ON/OFF 버튼
         binding.bPDR.setOnClickListener(view -> {
             PDR_ON_OFF = 1; //버튼 신호를 다른 Activity(UncalAccelermeter)에 보내기
             binding.AccLPF.setText("Step : Go!");
         });
         binding.bthUpload.setOnClickListener(view -> {
             if (dbKey) {
                 Toast.makeText(getApplicationContext(), "DB 업로드를 진행합니다", Toast.LENGTH_SHORT).show();
                 DataBaseParsing();
                 Toast.makeText(getApplicationContext(), "DB 업로드가 완료되었습니다", Toast.LENGTH_SHORT).show();
                 dbKey = false;
             }
             else Toast.makeText(getApplicationContext(), "이미 DB가 업로드된 상태입니다.", Toast.LENGTH_SHORT).show();
         });
////////////////////////경일대 수정 부분 종료 (23.09.04)////////////////////////////

    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this, Common.permission, 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "파일 다운로드를 위해 앱을 허용해 주세요", Toast.LENGTH_LONG).show();
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(permissionIntent);
            }
        }
    }

    /**
     * initSwitch, initScanner 메소드 인자 값의 순서가 중요(필수)
     * ex) lms -> LMScanner
     *     unAcc -> UncalAccelerometer
     *     ori -> Orientation
     */
    private void initScannerController() {
        scannerController = new ScannerController();
        scannerController.initScanner(
            new UncalAccelerometer(this, UNCAL_ACC_TITLE),
            new UncalGyroscope(this, UNCAL_GYRO_TITLE),
            new UncalMagnetic(this, UNCAL_MAG_TITLE),
            new Orientation(this, ORI_TITLE),
            new WifiScanner(this, WIFI_TITLE),
            new GnssScanner(this, GNSS_TITLE, (type, location) -> {}),
            new LMScanner(this, LOA_TITLE, (type, location) -> {})

        );

    }

    ////////////////////////경일대 추가 부분 시작 (23.08.28)////////////////////////////
    /** 0.01초마다 화면 출력 데이터 갱신 */
       @SuppressLint({"SetTextI18n", "DefaultLocale"})
       public  void step_c(){
           step1 = DetectPeck.step_c; // Before : DetectPeck.Stepout();
           stride1 = Stride.stride;   // Before : Stride.Stride_out();
           heading1=Heading.yaw;      // Before : Heading.Heading_Dist();
           heading1=heading1*r2d;
           PDR_p = PDR.pos;           // Before : PDR.PDR_Out();

           headingM= Mag_Heading.Yaw_m;
           headingM=headingM*r2d;

         // PDR 정보 실시간 출력
           binding.AccLPF2.setText("Step : "+step1+"\r\nStride : "+String.format("%.2f",stride1)+"(m)" +  //Stride(m)
                             "\r\nHeading (Gyro)   : "+String.format("%.2f",heading1)+"(deg)" +           //Gyro Heading (deg)
                             "\r\nHeading (Magnet) : "+String.format("%.2f",headingM)+"(deg)"+            //Magnet Heading (deg)
                             "\r\nIn/Out : "+ GpsLScanner.G_IO);  //PDR data : Stride, Heading(Gyro, Magnet) //In/Out 확인

           binding.AccLPF3.setText("PDR_G x : "+String.format("%.2f",PDR_p[0])+", y : " +String.format("%.2f",PDR_p[1])+            //PDR Local 좌표계
                         "\r\nWiFi_FingerPrinting \r\n Lon : "+ Positioning.Pos_F[0] +", \r\n Lat : " +Positioning.Pos_F[1]+", \r\n Floor : " +Positioning.Pos_F[2]);  // Fingerprint 위치 정보

         if(isActive) {
            refresh(10);// 0.01s delay update
        }
       }
    private void refresh(int milliseconds){
        final Handler handler = new Handler();
        final Runnable runnable = () -> step_c();
        handler.postDelayed(runnable,milliseconds);
    }


    ////////////////////////경일대 추가 부분 종료 (23.08.28)////////////////////////////
    private void toast(String msg) { runOnUiThread(() ->
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()); }
    private void DataBaseParsing() {  //DB Loading
        DataBase.initArray();
        DataBase.jsonParsing(this);
    }
}