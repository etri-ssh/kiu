package com.example.ccp.views;

import static com.example.ccp.common.MyApplication.backgroundDetector;
import static com.example.ccp.common.MyApplication.shared;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.ccp.R;
import com.example.ccp.common.BackgroundDetector;
import com.example.ccp.common.BottomToast;
import com.example.ccp.common.Common;
import com.example.ccp.common.map.InteriorMap;
import com.example.ccp.common.retrofit.model.BuildingMapResponse;
import com.example.ccp.common.retrofit.model.BuildingResponse;
import com.example.ccp.common.retrofit.model.CameraRequest;
import com.example.ccp.common.retrofit.model.CameraResponse;
import com.example.ccp.common.retrofit.model.RadiusMapResponse;
import com.example.ccp.databinding.ActivityMainBinding;
import com.example.ccp.module.model.ImagePositioning;

import com.example.ccp.module.pos.ImageModule;
import com.example.ccp.map_module.MM;
import com.example.ccp.map_module.model.JLine;
import com.example.ccp.module.DataCollection;
import com.example.ccp.module.model.BasePositioning;
import com.example.ccp.module.model.SignalPositioning;
import com.example.ccp.module.pos.FusedModule;
import com.example.ccp.module.pos.GNSSModule;
import com.example.ccp.module.pos.SignalModule;
import com.example.ccp.module.pos.AndGnssLModule;
import com.example.ccp.scan_module.ScanModule;
import com.example.ccp.web.WebAppClient;
import com.example.ccp.web.WebAppInterface;

import org.json.JSONArray;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private ActivityMainBinding binding;
    private int backCnt = 0;
    private String selectItem;

    // module
    private FusedModule fusedModule;
    private GNSSModule gnssModule;
    private SignalModule signalModule;
    private ImageModule imageModule;
    private ScanModule scanModule;
    private AndGnssLModule andGnssLModule;    // new GPS location module(fused 참고해서 수정필요)

    // thread
    private Thread mapThread;

    // js sync
    private final Object sync = new Object();

    // 묘듈 시작
    private final MutableLiveData<Boolean> moduleFlag = new MutableLiveData<>(true);

    // 자동 지도
    public static boolean autoFlag = true;
    private InteriorMap currentMap = null;

    // Map-matching
    private final MM mapMatching = new MM();

    // 상세 정보
    public static MutableLiveData<String> inContent = new MutableLiveData<>();

    private final ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if(result.getData() != null) {
                    BuildingResponse item = (BuildingResponse) result.getData().getSerializableExtra("item");
                    binding.tvSearch.setText(item.name);
                    getMapAPI(item.id);
                }
            }
        });

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        context = this;
        setContentView(binding.getRoot());
        backgroundDetector.addListener(new BackgroundDetector.Listener() {
            @Override
            public void onBecameForeground() {}
            @Override
            public void onBecameBackground() {
                moduleStopAll();
                finishAndRemoveTask();
                System.exit(0);
            }
        });

        binding.settingBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
            startActivity(intent);
        });
        binding.searchbar.setOnClickListener(view ->{
            Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
            startActivityResult.launch(intent);
        });

        binding.btnSelect.setOnClickListener(view -> {
            binding.tvInfoContent.setText("");
            binding.llInfo.setVisibility(View.GONE);
            if(binding.llSelect.getVisibility() == View.VISIBLE) {
                binding.llSelect.setVisibility(View.GONE);
                binding.btnSelect.setBackgroundDrawable(
                        ContextCompat.getDrawable(this, R.drawable.btn_bg_01));
            } else {
                binding.btnSelect.setBackgroundDrawable(
                        ContextCompat.getDrawable(this, R.drawable.btn_bg_02));
                binding.llSelect.setVisibility(View.VISIBLE);
            }
        });
        View.OnTouchListener touchListener = (view, motionEvent) -> {
            switch (motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN: {
                    LinearLayout layout = (LinearLayout) ((LinearLayout) view).getChildAt(1);
                    ((TextView)layout.getChildAt(0)).setTextColor(getColor(R.color.sky_blue));
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(!view.isPressed()) {
                        LinearLayout layout = (LinearLayout) ((LinearLayout) view).getChildAt(1);
                        ((TextView)layout.getChildAt(0)).setTextColor(getColor(R.color.black));
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:{
                    if(!view.isPressed()) break;
                    LinearLayout layout = (LinearLayout) ((LinearLayout) view).getChildAt(1);
                    TextView tv = ((TextView)layout.getChildAt(0));
                    tv.setTextColor(getColor(R.color.black));
                    selectItem = tv.getText().toString();
                    binding.btnSelect.setText(selectItem);
                    binding.btnSelect.setBackgroundDrawable(
                            ContextCompat.getDrawable(this, R.drawable.btn_bg_01));
                    binding.llSelect.setVisibility(View.GONE);
                    binding.btnInfo.setBackgroundDrawable(
                            ContextCompat.getDrawable(this, R.drawable.btn_bg_04));
                    sContentView = view;
                    break;
                }
            }
            return false;
        };
        binding.llAll.setOnTouchListener(touchListener);
        binding.llFused.setOnTouchListener(touchListener);
        binding.llGnss.setOnTouchListener(touchListener);
        binding.llSignal.setOnTouchListener(touchListener);
        binding.llImage.setOnTouchListener(touchListener);

        binding.btnInfo.setOnClickListener(view -> {
            if(selectItem != null) {
                if(binding.llInfo.getVisibility()==View.GONE){
                    binding.tvInfoTitle.setText(selectItem);
                    binding.llInfo.setVisibility(View.VISIBLE);
                    inContentTimer();
                } else { binding.llInfo.setVisibility(View.GONE); }
            } else { BottomToast.createToast(this, "측위 종류 선택 후 버튼이 활성화 됩니다").show(); }
        });

        // 측위 토글 이벤트
        binding.schAll.setOnCheckedChangeListener((compoundButton, b) ->
            jsCall(7, new HashMap<String, Object>() {{ put("type", Common.T_RESULT); put("flag", b); }}));
        binding.schFused.setOnCheckedChangeListener((compoundButton, b) ->
            jsCall(7, new HashMap<String, Object>() {{ put("type", Common.T_FUSED); put("flag", b); }}));
        binding.schGnss.setOnCheckedChangeListener((compoundButton, b) ->
            jsCall(7, new HashMap<String, Object>() {{ put("type", Common.T_GNSS); put("flag", b); }}));
        binding.schSignal.setOnCheckedChangeListener((compoundButton, b) ->
            jsCall(7, new HashMap<String, Object>() {{ put("type", Common.T_SIGNAL); put("flag", b); }}));
        binding.schImage.setOnCheckedChangeListener((compoundButton, b) ->
            jsCall(7, new HashMap<String, Object>() {{ put("type", Common.T_IMAGE); put("flag", b); }}));

        binding.ivInfoClose.setOnClickListener(view -> {
            binding.llInfo.setVisibility(View.GONE);
            inContentTimer.cancel();
            inContentTimer = null;
        });

        binding.capture.setOnClickListener(view -> {
            if(shared.getImageState()) {
                imageModule.captureImage(this);
            } else BottomToast.createToast(this, "영상 기반 측위가 현재 Off 되어 있습니다").show();
        });

        inContent.observe(this, i -> binding.tvInfoContent.setText(i));

        moduleFlag.observe(this, i -> {
            gnssModule.start(Common.GNSS);
            signalModule.start(shared.getSignalTimer());
            DataCollection.instance.resultStart(shared.getFinalTimer());
        });

        // 초기화
        initModule();
        initWebView();
        initMarkerAction();
        initMapThread();
        initCaptureObserve();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            backCnt += 1;
            if(backCnt > 1) finish();
        }
        return false;
    }

    private void initModule() {
        fusedModule = new FusedModule(this);

        imageModule = new ImageModule(binding.pv);

        gnssModule = new GNSSModule(this);
        signalModule = new SignalModule();

        scanModule = new ScanModule(this);
        scanModule.start();


        /** [ETRI] 추가 획득 부분 수정 진행중 (24.01.23)
         * 아래 module에 추가 수정 예정 중
         */

        andGnssLModule = new AndGnssLModule(this);
        andGnssLModule.start();

        ////////////////////////////////////////
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void initWebView() {
        Context context = this;
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.getSettings().setDomStorageEnabled(true);
        binding.webview.setWebViewClient(new WebAppClient());
        binding.webview.setNetworkAvailable(true);
        binding.webview.loadUrl("file:///android_asset/main.html");
        binding.webview.addJavascriptInterface(new WebAppInterface() {
            @Override
            @JavascriptInterface
            public void cameraAction(int visibility) { // js 에서 카메라 on/off 감지
                Common.log("addJavascriptInterface visibility : " + visibility);
                imageModule.startCamera(context);
                runOnUiThread(() -> binding.llCam.setVisibility(visibility));
            }

            @Override
            @JavascriptInterface
            public void locationAction(boolean flag) { // js 에서 mapMatching on/off 감지
                Common.log("flag : " + flag);
            }

            @Override
            @JavascriptInterface
            public void floorChangeAction(double floor) { // js 에서 일어난 층 변경 감지
                if(currentMap.currentFloor != floor) { // 현재 층과 js 에서 변경한 층이 서로 다를 경우
                    Common.log("JS 층 변경 감지 변경된 층 : " + floor + ", 현재 층 : " + currentMap.currentFloor);
                    currentMap.currentFloor = floor; // 현재 층 변경
                    currentMap.basePaths = currentMap.findFloorPaths(floor); // 경로 설정
                    readyMapMatching();
                }
            }

            @Override
            @JavascriptInterface
            public String currentPosition() {
                BasePositioning bp = DataCollection.instance.r1Last();
                return bp.lon+","+bp.lat;
            }
        }, "Android");
    }

    // 마커 찍는 쓰레드
    private void initMarkerAction() {
        DataCollection.instance.markerAction = bp -> {
            Common.log("[marker] bp : " + bp);  // initMarkerAction() 최초 선언시에 한번만 동작
           
            //jsCall은 Runnable로 선언되기 때문에 markerAction이 호출될때 마다 실행
            jsCall(4, new HashMap<String, Object>() {{
                put("type", bp.type);
                put("lon", bp.lon);
                put("lat", bp.lat);
            }});
        };
    }

    private void initMapThread() {
        mapThread = new Thread(() -> {
            while (true) {
                try {
                    synchronized (this) {
                        try {wait(Common.MAP);}
                        catch (Exception e) {Common.logW("markerThread wait Error");}
                    }
                    // 자동 층 전환
                    SignalPositioning sp = DataCollection.instance.signalLast();
                    if (currentMap != null && sp != null && sp.floor != 0 && currentMap.currentFloor != sp.floor) {
                        jsCall(2, new HashMap<String, Object>() {{
                            put("floor", sp.floor);
                            put("lon", sp.lon);
                            put("lat", sp.lat);
                        }});
                    }
                    // 자동 지도
                    BasePositioning bp;
                    if(sp == null) bp = DataCollection.instance.r1Last();
                    else bp = sp;
                    if(autoFlag) {
                        jsCall(5, new HashMap<String, Object>() {{
                            put("lon", bp.lon);
                            put("lat", bp.lat);
                        }});
                    }
                } catch (Exception e) {
                    Common.logW("mapThread wait Error"); e.printStackTrace();
                }
            }
        });
        mapThread.start();
    }

    // 이미지 캡처한 파일 갱신 시 영상 지점 측위 api 호출
    private void initCaptureObserve() {
        ImageModule.captureFile.observe(this, i -> {
            Common.log("[image] captureFile observe : " + i);
            jsCall(3, null); // 카메라 off
            try {
                String encoding = Base64.getEncoder().encodeToString(Files.readAllBytes(i.toPath()));
                CameraRequest cameraRequest = new CameraRequest();
                cameraRequest.image = encoding;
                Common.E_IMAGE_API.getCameraPoint(cameraRequest).enqueue(new Callback<CameraResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CameraResponse> call, @NonNull Response<CameraResponse> response) {
                        String msg = "Image Api Successful";
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                if(response.body().result) {
                                    DataCollection.instance.imagePut(new ImagePositioning(response.body()));
                                } else DataCollection.instance.imageFailPut(response.body().message);
                            } else DataCollection.instance.imageFailPut("Image Response Data Null");
                        } else msg = "Image Api Fail Successful";
                        BottomToast.createToast(context, msg).show();
                        if(!i.delete()) Common.log("[image] 사용 파일 삭제 실패");
                    }
                    @Override
                    public void onFailure(@NonNull Call<CameraResponse> call, @NonNull Throwable t) {
                        BottomToast.createToast(context, "데이터를 처리하지 못했습니다\n" + t.getMessage()).show();
                        if(!i.delete()) Common.logW("[image] 사용 파일 삭제 실패");
                    }
                });
            } catch(Exception e) {
                BottomToast.createToast(context, "Image Encoding Error\n" + e.getMessage()).show();
                if(!i.delete()) Common.logW("[image] 사용 파일 삭제 실패");
            }
        });
    }

    // 각 묘듈의 상세 정보 view 표기를 위한 변수 및 메서드
    private View sContentView; // 선택된 모듈 view
    private Timer inContentTimer; // 내용 갱신 및 반복을 위한 timer
    private void inContentTimer() { // 특정 간격으로 timer를 통해 갱신
        if(inContentTimer != null) inContentTimer.cancel();
        inContentTimer = new Timer();
        inContentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                StringBuilder msg = new StringBuilder();
                try {
                    if(sContentView == binding.llAll) {
                        msg.append("[count] ").append(DataCollection.instance.r1Size()).append("\n");
                        msg.append(DataCollection.instance.r1Last());
                    }else if(sContentView == binding.llFused) {
                        msg.append("[count] ").append(DataCollection.instance.fusedSize()).append("\n");
                        msg.append(DataCollection.instance.fusedLast());
                    } else if(sContentView == binding.llGnss) {
                        msg.append("[count] ").append(DataCollection.instance.gnssAllSize()).append("\n");
                        msg.append(DataCollection.instance.gnssLast());

//                        msg.append("[success count] ").append(DataCollection.instance.gnssSuccessCount).append("\n");
//                        msg.append("[fail count] ").append(DataCollection.instance.gnssFailCount).append("\n");
//                        GnssPositioning gp = DataCollection.instance.gnssFail();
//                        if(gp == null) msg.append(DataCollection.instance.gnssLast());
//                        else msg.append(gp);
                    } else if(sContentView == binding.llSignal) {
                        msg.append("[count] ").append(DataCollection.instance.signalAllSize()).append("\n");
                        msg.append(DataCollection.instance.signalLast());

//                        msg.append("[success count] ").append(DataCollection.instance.signalSuccessCount).append("\n");
//                        msg.append("[fail count] ").append(DataCollection.instance.signalFailCount).append("\n");
//                        SignalPositioning sp = DataCollection.instance.signalFail();
//                        if(sp == null) msg.append(DataCollection.instance.signalLast());
//                        else msg.append(sp);
                    } else if(sContentView == binding.llImage) {
                        msg.append("[count] ").append(DataCollection.instance.imageAllSize()).append("\n");
                        msg.append(DataCollection.instance.imageLast());

//                        msg.append("[success count] ").append(DataCollection.instance.imageSuccessCount).append("\n");
//                        msg.append("[fail count] ").append(DataCollection.instance.imageFailCount).append("\n");
//                        ImagePositioning ip = DataCollection.instance.imageFail();
//                        if(ip == null) msg.append(DataCollection.instance.imageLast());
//                        else msg.append(ip);
                    }
                } catch(Exception e) { msg.append("error : ").append(e); e.printStackTrace(); }
                finally { inContent.postValue(msg.toString()); }
            }
        }, 0, 500);
    }

    @Override
    protected void onDestroy() {
        moduleStopAll();
        super.onDestroy();

    }

    private void moduleStopAll() {
        Common.log("moduleStopAll Start");
        try { mapThread.interrupt(); }
        catch(Exception e) { e.printStackTrace(); }
        try { fusedModule.stop(); }
        catch(Exception e) { e.printStackTrace(); }
        try { gnssModule.stop(); }
        catch(Exception e) { e.printStackTrace(); }
        try { signalModule.stop(); }
        catch(Exception e) { e.printStackTrace(); }
        try { DataCollection.instance.stop(); }
        catch(Exception e) { e.printStackTrace(); }
        try { scanModule.stop(); }
        catch(Exception e) { e.printStackTrace(); }
        try { Thread.sleep(500); }
        catch(Exception e) { e.printStackTrace(); }


        //[ETRI] ???? 추가 테스트(24.01.23)
        try { andGnssLModule.stop(); }
        catch(Exception e) { e.printStackTrace(); }

        Common.log("moduleStopAll End");
    }

    // 맵매칭 준비를 위한 데이터 설정
    private void readyMapMatching() {
        mapMatching.resetData(); // 기존 데이터 리셋
        jsCall(6, new HashMap<String, Object>() {{ // 실내 지도 좌표변환(5186 -> 4326)
            put("markers", currentMap.basePaths);
        }});
    }

    // js 좌표 변환(최종: EPSG:5186)
    private void transformPoint(double lon, double lat) {
        String script = "javascript:transformPoint";
        script += "(" + lon + "," + lat + "," + "'EPSG:4326')";
        binding.webview.evaluateJavascript(script, v -> {
            String convert = v.substring(1, v.length() - 1);
            String[] arr = convert.split(",");
            Common.log("arr : " + Arrays.toString(arr));
            radiusApi(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
        });
    }

    // js 좌표 변환(5186 -> 4326)
    private void transformPointAll(JSONArray data) {
        String script = "javascript:transformPointAll";
        script += "(" + data + ")";
        binding.webview.evaluateJavascript(script, v -> {
            List<JLine> lines = currentMap.getStringToLines(v);
            mapMatching.inputLineAll(lines); // 맵 매칭 경로 넣기
        });
    }

    // 좌표에 따른 반경 건물 검색
    private void radiusApi(double x, double y) {
        int radius = shared.getMapRadiusValue(getResources().getStringArray(R.array.mapRadius));
        if(Common.H_MAP_API == null) return;
        Common.H_MAP_API.getRadiusMap(x, y, radius).enqueue(new Callback<List<RadiusMapResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RadiusMapResponse>> call, @NonNull Response<List<RadiusMapResponse>> response) {
                Common.log("getRadiusMap onResponse");
                if(response.isSuccessful()) {
                    if(response.body() != null && !response.body().isEmpty()) {
                        if(response.body().size() == 1) { // 결과 값 갯수가 1일 경우
                            // 현재 표출되고 있는 지도가 없거나 검색한 건물이 표출하고 있는 건물과 같지 않을 경우
                            if(currentMap == null || currentMap.buildingId != response.body().get(0).buildingId) {
                                getMapAPI(response.body().get(0).buildingId); // buildingId 기준 새로운 지도 api 호출
                            }
                        } else Common.log("복수의 건물이 검색되었습니다");
                    } else Common.log("주변에 검색된 건물이 없습니다");
                } else Common.log("Radius Building Api Fail Successful");
            }
            @Override
            public void onFailure(@NonNull Call<List<RadiusMapResponse>> call, @NonNull Throwable t) {
                Common.log("반경 건물 통신에 이슈가 발생했습니다 : " + t);
                t.printStackTrace();
            }
        });
    }

    // id 기준, 실내 지도 가져 오는 api 호출
    private void getMapAPI(int id) {
        if(Common.H_MAP_API == null) return;
        Common.H_MAP_API.getBuildingMap(id).enqueue(new Callback<List<BuildingMapResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<BuildingMapResponse>> call, @NonNull Response<List<BuildingMapResponse>> response) {
                if(response.isSuccessful()) {
                    if(response.body() != null && !response.body().isEmpty()) {
                        autoFlag = false; // 자동 반경 검색 멈춤
                        currentMap = new InteriorMap(id, response.body());
                        jsCall(1, new HashMap<String, Object>() {{ // 지도 뿌리기
                            put("map", currentMap.getFloorMapList());
                        }});
                        readyMapMatching();
                    }
                } else BottomToast.createToast(context, "실내 지도를 가져오는데 오류가 발생했습니다").show();
            }
            @Override
            public void onFailure(@NonNull Call<List<BuildingMapResponse>> call, @NonNull Throwable t) {
                binding.tvSearch.setText("");
                BottomToast.createToast(context, "실내 지도를 가져오지 못했습니다\n" + t).show();
            }
        });
    }

    private synchronized void jsCall(int type, Map<String, Object> data) {
        Runnable r = () -> {
            synchronized(sync) {
                try {
                    if(type == 1) { // 실내 지도 표시
                        setMapLayer((JSONArray) data.get("map"));
                    } else if(type == 2) { // 자동 층 전환
                        setFloorChange((double) data.get("floor"), (double) data.get("lon"), (double) data.get("lat"));
                    } else if(type == 3) { // 카메라 off
                        setCameraOff();
                    } else if(type == 4) { // 단일 마커 표시
                        doSingleMarker((String) data.get("type"), (double) data.get("lon"), (double) data.get("lat"));
                    } else if(type == 5) { // 자동 실내 지도
                        transformPoint((double) data.get("lon"), (double) data.get("lat"));
                    } else if(type == 6) { // 맵 매칭을 위한 실내 좌표 변경
                        transformPointAll((JSONArray) data.get("markers"));
                    } else if(type == 7) {
                        switchPosToggle((String) data.get("type"), (boolean) data.get("flag"));
                    }
                } catch(Exception e) {
                    Common.logW("[jsCall] jsCall error : " + e);
                    e.printStackTrace();
                }
            }
        };
        runOnUiThread(r);
    }

    // type 에 따른 토글 이벤트
    private void switchPosToggle(String type, boolean flag) {
        binding.webview.loadUrl("javascript:switchPosToggle('" + type + "',"+flag+")");
    }

    // 건물 실내 지도 데이터 셋팅
    private void setMapLayer(JSONArray data) {
        binding.webview.loadUrl("javascript:setMapLayer(" + data + ")");
    }

    // 자동 층 전환에 따른 실내 지도 변경
    private void setFloorChange(double floorNumber, double lon, double lat) {
        binding.webview.loadUrl("javascript:findFloorChange("+floorNumber+","+lon+","+lat+")");
    }

    // 카메라 off
    private void setCameraOff() {
        binding.webview.loadUrl("javascript:capture_icon_click()");
    }

    private void doSingleMarker(String type, double lon, double lat){
        binding.webview.loadUrl("javascript:doSingleMarker('"+type+"',"+lon+","+lat+")");
    }

    // initMarkerAction에 의해서 action이 구동될때 마다 jsCall이 실행
    public interface MarkerAction { void action(BasePositioning bp); }

}