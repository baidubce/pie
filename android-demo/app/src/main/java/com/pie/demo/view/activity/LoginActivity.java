package com.pie.demo.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.pie.demo.Constants;
import com.pie.demo.R;
import com.pie.demo.utils.SpUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private TextView mTVmm, mTvchToken, mTVtoken, mTvtime, mTVpass;
    private EditText mEtAccout, mEtpwd, mEttoken;
    private Button mBtLogin;
    private LinearLayout mLltoken, mLlpwd, mLlaccout, mLltokentime;
    private boolean isShowPwd = true;
    private boolean isShowToken = false;
    private boolean isShowNo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        initView();
        initSp();
        initLisener();
    }


    private void initLisener() {
        mTVmm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTVmm.setBackgroundResource(R.color.colorblue);
                mTVmm.setTextColor(getResources().getColor(R.color.white));
                mTVtoken.setBackgroundResource(R.color.white);
                mTVtoken.setTextColor(getResources().getColor(R.color.black));
                mTVpass.setBackgroundResource(R.color.white);
                mTVpass.setTextColor(getResources().getColor(R.color.black));
                mLltoken.setVisibility(View.GONE);
                mLlpwd.setVisibility(View.VISIBLE);
                mLlaccout.setVisibility(View.VISIBLE);
                mLltokentime.setVisibility(View.VISIBLE);
                isShowPwd = true;
                isShowToken = false;
                isShowNo = false;
            }
        });

        mTVtoken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTVtoken.setBackgroundResource(R.color.colorblue);
                mTVtoken.setTextColor(getResources().getColor(R.color.white));
                mTVmm.setBackgroundResource(R.color.white);
                mTVmm.setTextColor(getResources().getColor(R.color.black));
                mTVpass.setBackgroundResource(R.color.white);
                mTVpass.setTextColor(getResources().getColor(R.color.black));
                mLltoken.setVisibility(View.VISIBLE);
                mLlpwd.setVisibility(View.GONE);
                mLlaccout.setVisibility(View.VISIBLE);
                mLltokentime.setVisibility(View.VISIBLE);
                isShowPwd = false;
                isShowToken = true;
                isShowNo = false;
            }
        });

        mTVpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTVpass.setBackgroundResource(R.color.colorblue);
                mTVpass.setTextColor(getResources().getColor(R.color.white));
                mTVmm.setBackgroundResource(R.color.white);
                mTVmm.setTextColor(getResources().getColor(R.color.black));
                mTVtoken.setBackgroundResource(R.color.white);
                mTVtoken.setTextColor(getResources().getColor(R.color.black));
                mLltoken.setVisibility(View.GONE);
                mLlpwd.setVisibility(View.GONE);
                mLlaccout.setVisibility(View.GONE);
                mLltokentime.setVisibility(View.GONE);
                isShowPwd = false;
                isShowToken = false;
                isShowNo = true;
            }
        });

        mTvchToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                TimePickerView pvTime = new TimePickerBuilder(LoginActivity.this, new OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {//选中事件回调
                        SimpleDateFormat format = new SimpleDateFormat(com.baidu.acu.pie.model.Constants.UTC_DATE_TIME_FORMAT);
                        String time = format.format(date);
                        mTvtime.setText(time);
                    }
                })
                        .setType(new boolean[]{true, true, true, true, true, true})//分别对应年月日时分秒，默认全部显示
                        .setLabel("年", "月", "日", "时", "分", "秒")
                        .build();
                pvTime.show();
            }
        });
        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowPwd) {
                    String accout = mEtAccout.getText().toString().trim();
                    if (TextUtils.isEmpty(accout)) {
                        Toast.makeText(LoginActivity.this, "请填写账号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String pwd = mEtpwd.getText().toString().trim();
                    if (TextUtils.isEmpty(pwd)) {
                        Toast.makeText(LoginActivity.this, "请填写密码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String time = mTvtime.getText().toString().trim();

                    SpUtils.getInstance().putString(Constants.ACCOUTONE, accout);
                    SpUtils.getInstance().putString(Constants.ACCOUTTWO, accout);
                    SpUtils.getInstance().putString(Constants.ACCOUTHREE, accout);
                    SpUtils.getInstance().putString(Constants.PWDONE, pwd);
                    SpUtils.getInstance().putString(Constants.PWDTWO, pwd);
                    SpUtils.getInstance().putString(Constants.PWDTHREE, pwd);
                    SpUtils.getInstance().putString(Constants.TIMEONE, time);
                    SpUtils.getInstance().putString(Constants.TIMETWO, time);
                    SpUtils.getInstance().putString(Constants.TIMETHREE, time);

                    SpUtils.getInstance().putString(Constants.TOKENONE, null);
                    SpUtils.getInstance().putString(Constants.TOKENTWO, null);
                    SpUtils.getInstance().putString(Constants.TOKENTHREE, null);

                    SpUtils.getInstance().putString(Constants.LOGINSTYLEONE, "loginpwd");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETWO, "loginpwd");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETHREE, "loginpwd");
                }


                if (isShowToken) {
                    String accout = mEtAccout.getText().toString().trim();
                    if (TextUtils.isEmpty(accout)) {
                        Toast.makeText(LoginActivity.this, "请填写账号", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String token = mEttoken.getText().toString().trim();
                    if (TextUtils.isEmpty(token)) {
                        Toast.makeText(LoginActivity.this, "请填写token", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String time = mTvtime.getText().toString().trim();

                    SpUtils.getInstance().putString(Constants.ACCOUTONE, accout);
                    SpUtils.getInstance().putString(Constants.ACCOUTTWO, accout);
                    SpUtils.getInstance().putString(Constants.ACCOUTHREE, accout);
                    SpUtils.getInstance().putString(Constants.PWDONE, null);
                    SpUtils.getInstance().putString(Constants.PWDTWO, null);
                    SpUtils.getInstance().putString(Constants.PWDTHREE, null);
                    SpUtils.getInstance().putString(Constants.TIMEONE, time);
                    SpUtils.getInstance().putString(Constants.TIMETWO, time);
                    SpUtils.getInstance().putString(Constants.TIMETHREE, time);

                    SpUtils.getInstance().putString(Constants.TOKENONE, token);
                    SpUtils.getInstance().putString(Constants.TOKENTWO, token);
                    SpUtils.getInstance().putString(Constants.TOKENTHREE, token);

                    SpUtils.getInstance().putString(Constants.LOGINSTYLEONE, "logintoken");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETWO, "logintoken");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETHREE, "logintoken");
                }

                if (isShowNo) {
                    SpUtils.getInstance().putString(Constants.ACCOUTONE, null);
                    SpUtils.getInstance().putString(Constants.ACCOUTTWO, null);
                    SpUtils.getInstance().putString(Constants.ACCOUTHREE, null);
                    SpUtils.getInstance().putString(Constants.PWDONE, null);
                    SpUtils.getInstance().putString(Constants.PWDTWO, null);
                    SpUtils.getInstance().putString(Constants.PWDTHREE, null);
                    SpUtils.getInstance().putString(Constants.TIMEONE, null);
                    SpUtils.getInstance().putString(Constants.TIMETWO, null);
                    SpUtils.getInstance().putString(Constants.TIMETHREE, null);

                    SpUtils.getInstance().putString(Constants.TOKENONE, null);
                    SpUtils.getInstance().putString(Constants.TOKENTWO, null);
                    SpUtils.getInstance().putString(Constants.TOKENTHREE, null);

                    SpUtils.getInstance().putString(Constants.LOGINSTYLEONE, "loginno");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETWO, "loginno");
                    SpUtils.getInstance().putString(Constants.LOGINSTYLETHREE, "loginno");
                }


                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView() {
        mTVmm = findViewById(R.id.mTVmm);
        mTVtoken = findViewById(R.id.mTVtoken);
        mEtAccout = findViewById(R.id.mEtAccout);
        mEtpwd = findViewById(R.id.mEtpwd);
        mEttoken = findViewById(R.id.mEttoken);
        mTvchToken = findViewById(R.id.mTvchToken);
        mTvtime = findViewById(R.id.mTvtime);
        mBtLogin = findViewById(R.id.mBtLogin);
        mLltoken = findViewById(R.id.mLltoken);
        mLlpwd = findViewById(R.id.mLlpwd);
        mTVpass = findViewById(R.id.mTVpass);
        mLlaccout = findViewById(R.id.mLlaccout);
        mLltokentime = findViewById(R.id.mLltokentime);

        mEtAccout.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mEttoken.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        SimpleDateFormat format = new SimpleDateFormat(com.baidu.acu.pie.model.Constants.UTC_DATE_TIME_FORMAT);
        long currentTimeMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
        Date date = new Date(currentTimeMillis);
        String time = format.format(date);
        mTvtime.setText(time);
    }


    private void initSp() {
        String accout = SpUtils.getInstance().getString(Constants.ACCOUTONE);
        String pwd = SpUtils.getInstance().getString(Constants.PWDONE);
        String token = SpUtils.getInstance().getString(Constants.TOKENONE);
        if (!TextUtils.isEmpty(accout)) {
            mEtAccout.setText(accout);
        }
        if (!TextUtils.isEmpty(pwd)) {
            mEtpwd.setText(pwd);
        }
        if (!TextUtils.isEmpty(token)) {
            mEttoken.setText(token);
        }
    }

    private void hideSoftKeyboard() {
        View localView = getCurrentFocus();
        if (localView != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(localView.getWindowToken(), 2);
        }
    }
}
