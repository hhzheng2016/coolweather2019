package com.weather.android;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weather.android.db.City;
import com.weather.android.db.County;
import com.weather.android.db.Province;
import com.weather.android.util.HttpUtil;
import com.weather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;
    //选中的省、市、县
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    //当前Level
    private int currentLevel;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        mListView = view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id)->{
            if(currentLevel==LEVEL_PROVINCE){
                selectedProvince=mProvinceList.get(position);
                queryCities();
            }else if(currentLevel==LEVEL_CITY){
                selectedCity=mCityList.get(position);
                queryCounties();
            }
        });

        backButton.setOnClickListener((view)->{
            if(currentLevel==LEVEL_COUNTY){
                queryCities();
            }else if(currentLevel==LEVEL_CITY){
                queryProvinces();
            }
        });

        queryProvinces();
        Log.d(TAG, "onActivityCreated: finished");
    }

    /**
     * 查询所有的省
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0) {
            dataList.clear();
            for (Province province : mProvinceList) {
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        mCityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            dataList.clear();
            for (City city : mCityList) {
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        mCountyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (mCountyList.size() > 0) {
            dataList.clear();
            for (County county : mCountyList) {
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }


    private void queryFromServer(String address, final String type) {
        showProgressDiag();
        
        HttpUtil.sendOkHttpRequest( address, new Callback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: "+ e.toString());
                getActivity().runOnUiThread(()->{
                    closeProgressDialog();
                    Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                String responseText = response.body().toString();
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountryResponse(responseText, selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    //打开关闭对话框
    private void closeProgressDialog() {
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDiag() {
        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCancelable(false);
        }

    }
}
