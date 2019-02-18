package com.pie.demo.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.acu.pie.model.AsrProduct;
import com.pie.demo.Constants;
import com.pie.demo.R;
import com.pie.demo.utils.SpUtils;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbarSetting;
    private EditText mEtIp;
    private EditText mEtPort;
    private Button mBT;
    private RadioGroup mRadioGroup;
    private String flag;
    private int checkedRadioButtonId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        flag = getIntent().getStringExtra("flag");


        initView();

        setSupportActionBar(mToolbarSetting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initSp();
        initLisener();
        initRadioGroup();
    }


    private void initView() {
        mToolbarSetting = findViewById(R.id.mToolbarSetting);
        mEtIp = findViewById(R.id.mEtIp);
        mEtPort = findViewById(R.id.mEtPort);
        mBT = findViewById(R.id.mBT);
        mRadioGroup = findViewById(R.id.mRadioGroup);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSp() {

        switch (flag) {
            case "one":
                String oneAddress = SpUtils.getInstance().getString(Constants.ONEADDRESS);
                String onePort = SpUtils.getInstance().getString(Constants.ONEPORT);
                if (!TextUtils.isEmpty(oneAddress) && !TextUtils.isEmpty(onePort)) {
                    mEtIp.setText(oneAddress);
                    mEtPort.setText(onePort);
                }
                break;
            case "two":
                String twoAddress = SpUtils.getInstance().getString(Constants.TWOADDRESS);
                String twoPort = SpUtils.getInstance().getString(Constants.TWOPORT);
                if (!TextUtils.isEmpty(twoAddress) && !TextUtils.isEmpty(twoPort)) {
                    mEtIp.setText(twoAddress);
                    mEtPort.setText(twoPort);
                }
                break;
            case "three":
                String threeAddress = SpUtils.getInstance().getString(Constants.THREEADDRESS);
                String threePort = SpUtils.getInstance().getString(Constants.THREEPORT);
                if (!TextUtils.isEmpty(threeAddress) && !TextUtils.isEmpty(threePort)) {
                    mEtIp.setText(threeAddress);
                    mEtPort.setText(threePort);
                }
                break;
        }
    }

    private void initLisener() {

        mBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = mEtIp.getText().toString().trim();
                String port = mEtPort.getText().toString().trim();

                switch (flag) {
                    case "one":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port) && checkedRadioButtonId != -1) {
                            SpUtils.getInstance().putString(Constants.ONEADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.ONEPORT, port);
                            SpUtils.getInstance().putInt(Constants.ONEASRPRODUCT, checkedRadioButtonId);
                        }
                        break;
                    case "two":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port) && checkedRadioButtonId != -1) {
                            SpUtils.getInstance().putString(Constants.TWOADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.TWOPORT, port);
                            SpUtils.getInstance().putInt(Constants.TWOASRPRODUCT, checkedRadioButtonId);
                        }
                        break;
                    case "three":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port) && checkedRadioButtonId != -1) {
                            SpUtils.getInstance().putString(Constants.THREEADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.THREEPORT, port);
                            SpUtils.getInstance().putInt(Constants.THREEASRPRODUCT, checkedRadioButtonId);
                        }
                        break;
                }

                finish();

            }
        });
    }


    private void initRadioGroup() {

        int isCheckId = -1;

        switch (flag) {
            case "one":
                int oneAsr = SpUtils.getInstance().getInt(Constants.ONEASRPRODUCT);
                isCheckId = oneAsr;
                break;
            case "two":
                int twoAsr = SpUtils.getInstance().getInt(Constants.TWOASRPRODUCT);
                isCheckId = twoAsr;
                break;
            case "three":
                int threeAsr = SpUtils.getInstance().getInt(Constants.THREEASRPRODUCT);
                isCheckId = threeAsr;
                break;
        }
        mRadioGroup.removeAllViews();
        AsrProduct[] values = AsrProduct.values();
        for (int i = 0; i < values.length; i++) {
            RadioButton radioButton = new RadioButton(SettingActivity.this);
            radioButton.setId(i + 1);
            radioButton.setText(values[i].getName());
            mRadioGroup.addView(radioButton);
        }
        if (isCheckId != -1) {
            mRadioGroup.check(isCheckId);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedRadioButtonId = checkedId;
            }
        });
    }
}
