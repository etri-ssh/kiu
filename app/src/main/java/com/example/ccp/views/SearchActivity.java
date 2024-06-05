package com.example.ccp.views;

import static com.example.ccp.common.Common.H_MAP_API;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ccp.adapter.SearchAdapter;
import com.example.ccp.common.BottomToast;
import com.example.ccp.common.Common;
import com.example.ccp.common.retrofit.model.BuildingResponse;
import com.example.ccp.databinding.ActivitySearchBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new SearchAdapter(item -> {
            Intent intent = getIntent();
            intent.putExtra("item", item);
            setResult(RESULT_OK, intent);
            finish();
        });
        binding.rcvSearch.setLayoutManager(new LinearLayoutManager(getApplication(), LinearLayoutManager.VERTICAL, false));
        binding.rcvSearch.setAdapter(adapter);

        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void initEvent() {
        binding.ivSearch.setOnClickListener(view -> {
            if(binding.etvSearch.getText() != null) {
                String key = binding.etvSearch.getText().toString();
                Call<List<BuildingResponse>> call = H_MAP_API.getBuildings(key);
                call.enqueue(new Callback<List<BuildingResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<BuildingResponse>> call, @NonNull Response<List<BuildingResponse>> response) {
                        if(response.isSuccessful()) adapter.update(response.body());
                        else BottomToast.createToast(getApplication(), "검색된 실내 공간이 확인 되지 않습니다").show();
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<BuildingResponse>> call, @NonNull Throwable t) { Common.log("getBuildings Fail : " + t); }
                });
            } else BottomToast.createToast(this, "대상 실내 공간을 입력해 주세요").show();
        });

        binding.ivClose.setOnClickListener(view -> finish());
    }
}