package com.joe.dramaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.joe.dramaapp.bean.DramaBean;
import com.joe.dramaapp.databinding.ActivityMainBinding;
import com.joe.dramaapp.db.Drama;
import com.joe.dramaapp.db.DramaDatabase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    ActivityMainBinding activityMainBinding;
    ArrayList<DramaBean> alDramaBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        //RESTFUL API
        getDramaDataByOkhttp();

        if(alDramaBean != null)
        {
            //有資料就塞DB
            saveToDB(alDramaBean);
        }
    }

    private void getDramaDataByOkhttp() {
        //抓戲劇資料
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(getString(R.string.drama_url)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "[RESP]onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String json = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "[RESP]onResponse: " + json);

                        //解析中...
                        JSONArray array = null;
                        try {
                            JSONObject obj = new JSONObject(json);
                            array = obj.getJSONArray("data");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        parseGSON(array.toString());
                    }
                });
            }
        });
    }

    private void saveToDB(final ArrayList<DramaBean> alDramaBean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(DramaBean dramaBean : alDramaBean)
                {
                    DramaDatabase.getInstance(MainActivity.this).dramaDao()
                            .insert(new Drama(dramaBean.getDramaId(), dramaBean.getName(), dramaBean.getTotalViews(),
                                    dramaBean.getCreatedAt(), dramaBean.getThumbUrl(), dramaBean.getRating()));
                }
            }
        });
    }

    private void parseGSON(String json) {
        Gson gson = new Gson();
        alDramaBean = gson.fromJson(json, new TypeToken<ArrayList<DramaBean>>(){}.getType());

        processAdapterViews();

    }

    private void processAdapterViews() {
        DramaAdapter dramaAdapter = new DramaAdapter(this, alDramaBean, onClickItemListener);
        activityMainBinding.rvDramalist.setAdapter(dramaAdapter);
        activityMainBinding.rvDramalist.setHasFixedSize(true);
        activityMainBinding.rvDramalist.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    OnClickItemListener onClickItemListener = new OnClickItemListener() {
        @Override
        public void onClickDramaItem(DramaBean bean) {
            GoToDramaInfo(bean);

        }
    };

    private void GoToDramaInfo(DramaBean bean) {
        Intent intent = new Intent(this, DramaInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("intent_drama_bean", bean);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
