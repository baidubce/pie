package com.pie.demo.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

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
    private Switch mSwitch;
    private boolean mSwitchIsChecked;
    private String flag;
    private int checkedRadioButtonId = -1;

    private Button mBTTTS;
    private EditText mEtPortIpTTS, mEtSpd, mEtPit, mEtVol;

    private boolean isChanged = false;
    private AsrProduct[] values;
    private int isCheckId = -1;

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
        mSwitch = findViewById(R.id.mSwitch);


        this.mEtPortIpTTS = ((EditText) findViewById(R.id.mEtPortIpTTS));
        this.mBTTTS = ((Button) findViewById(R.id.mBTTTS));
        mEtSpd = findViewById(R.id.mEtSpd);
        mEtPit = findViewById(R.id.mEtPit);
        mEtVol = findViewById(R.id.mEtVol);

        LinearLayout localLinearLayout1 = (LinearLayout) findViewById(R.id.mLOne);
        LinearLayout localLinearLayout2 = (LinearLayout) findViewById(R.id.mLTwo);
        if ("tts".equals(this.flag)) {
            localLinearLayout1.setVisibility(View.GONE);
            localLinearLayout2.setVisibility(View.VISIBLE);
        } else {
            localLinearLayout1.setVisibility(View.VISIBLE);
            localLinearLayout2.setVisibility(View.GONE);
        }
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

                boolean switchone = SpUtils.getInstance().getBool(Constants.SWITCHISCHECKEDONE);
                mSwitchIsChecked = switchone;
                Log.e("tag", switchone + "");
                mSwitch.setChecked(switchone);

                break;
            case "two":
                String twoAddress = SpUtils.getInstance().getString(Constants.TWOADDRESS);
                String twoPort = SpUtils.getInstance().getString(Constants.TWOPORT);
                if (!TextUtils.isEmpty(twoAddress) && !TextUtils.isEmpty(twoPort)) {
                    mEtIp.setText(twoAddress);
                    mEtPort.setText(twoPort);
                }

                boolean switchtwo = SpUtils.getInstance().getBool(Constants.SWITCHISCHECKEDTWO);
                mSwitchIsChecked = switchtwo;
                mSwitch.setChecked(switchtwo);

                break;
            case "three":
                String threeAddress = SpUtils.getInstance().getString(Constants.THREEADDRESS);
                String threePort = SpUtils.getInstance().getString(Constants.THREEPORT);
                if (!TextUtils.isEmpty(threeAddress) && !TextUtils.isEmpty(threePort)) {
                    mEtIp.setText(threeAddress);
                    mEtPort.setText(threePort);
                }

                boolean switchthree = SpUtils.getInstance().getBool(Constants.SWITCHISCHECKEDTHREE);
                mSwitchIsChecked = switchthree;
                mSwitch.setChecked(switchthree);

                break;
        }


        String str1 = SpUtils.getInstance().getString(Constants.SERVER_IP_ADDR_PORT_TTS);
        this.mEtPortIpTTS.setText(str1);

        int spd = SpUtils.getInstance().getInt(Constants.SPD);
        int pit = SpUtils.getInstance().getInt(Constants.PIT);
        int vol = SpUtils.getInstance().getInt(Constants.VOl);

        mEtSpd.setText(String.valueOf(spd));
        mEtPit.setText(String.valueOf(pit));
        mEtVol.setText(String.valueOf(vol));

    }

    private void initLisener() {


        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSwitchIsChecked = isChecked;
            }
        });

        mBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = mEtIp.getText().toString().trim();
                String port = mEtPort.getText().toString().trim();

                switch (flag) {
                    case "one":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                            SpUtils.getInstance().putString(Constants.ONEADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.ONEPORT, port);
                            if (isChanged) {
                                SpUtils.getInstance().putInt(Constants.ONEASRPRODUCT, checkedRadioButtonId - 1);
                            }
                            SpUtils.getInstance().putBool(Constants.SWITCHISCHECKEDONE, mSwitchIsChecked);
                        }
                        break;
                    case "two":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                            SpUtils.getInstance().putString(Constants.TWOADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.TWOPORT, port);
                            if (isChanged) {
                                SpUtils.getInstance().putInt(Constants.TWOASRPRODUCT, checkedRadioButtonId - 1);
                            }
                            SpUtils.getInstance().putBool(Constants.SWITCHISCHECKEDTWO, mSwitchIsChecked);
                        }
                        break;
                    case "three":
                        if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                            SpUtils.getInstance().putString(Constants.THREEADDRESS, ip);
                            SpUtils.getInstance().putString(Constants.THREEPORT, port);
                            if (isChanged) {
                                SpUtils.getInstance().putInt(Constants.THREEASRPRODUCT, checkedRadioButtonId - 1);
                            }
                            SpUtils.getInstance().putBool(Constants.SWITCHISCHECKEDTHREE, mSwitchIsChecked);
                        }
                        break;
                }

                finish();

            }
        });

        mBTTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ipAndport = mEtPortIpTTS.getText().toString().trim();

                if (!TextUtils.isEmpty(ipAndport)) {
                    SpUtils.getInstance().putString(Constants.SERVER_IP_ADDR_PORT_TTS, ipAndport);
                }

                String spd = mEtSpd.getText().toString().trim();
                String pit = mEtPit.getText().toString().trim();
                String vol = mEtVol.getText().toString().trim();

                if (!TextUtils.isEmpty(spd)) {
                    SpUtils.getInstance().putInt(Constants.SPD, Integer.parseInt(spd));
                }
                if (!TextUtils.isEmpty(pit)) {
                    SpUtils.getInstance().putInt(Constants.PIT, Integer.parseInt(pit));

                }
                if (!TextUtils.isEmpty(vol)) {
                    SpUtils.getInstance().putInt(Constants.VOl, Integer.parseInt(vol));

                }

                finish();
            }
        });
    }


    private void initRadioGroup() {

        switch (flag) {
            case "one":
                int oneAsr = SpUtils.getInstance().getInt(Constants.ONEASRPRODUCT);
                isCheckId = oneAsr;
                checkedRadioButtonId = oneAsr;
                break;
            case "two":
                int twoAsr = SpUtils.getInstance().getInt(Constants.TWOASRPRODUCT);
                isCheckId = twoAsr;
                checkedRadioButtonId = twoAsr;
                break;
            case "three":
                int threeAsr = SpUtils.getInstance().getInt(Constants.THREEASRPRODUCT);
                isCheckId = threeAsr;
                checkedRadioButtonId = threeAsr;
                break;
        }
        mRadioGroup.removeAllViews();
        values = AsrProduct.values();
        for (int i = 0; i < values.length; i++) {
            RadioButton radioButton = new RadioButton(SettingActivity.this);
            radioButton.setId(i + 1);
            radioButton.setText(values[i].getName() + "(" + values[i].getSampleRate() + ")");
            mRadioGroup.addView(radioButton);
        }
        if (isCheckId != -1) {
            mRadioGroup.check(isCheckId + 1);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isChanged = true;
                checkedRadioButtonId = checkedId;

            }
        });
    }
}
