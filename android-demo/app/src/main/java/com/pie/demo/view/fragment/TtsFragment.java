package com.pie.demo.view.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.pie.demo.Constants;
import com.pie.demo.R;
import com.pie.demo.base.BaseFragment;
import com.pie.demo.listener.MyOnSeekChangeListener;
import com.pie.demo.manager.MediaPlayerManeger;
import com.pie.demo.manager.MediaPlayerManegerinterface;
import com.pie.demo.retrofit.RetrofitClient;
import com.pie.demo.utils.SpUtils;
import com.pie.demo.view.weiget.VoiceImgView;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TtsFragment extends BaseFragment {

    private VoiceImgView mButton;
    private EditText mEditText;
    private EditText mEtPortIpTTS;
    private IndicatorSeekBar mSeekBarOne, mSeekBarTwo, mSeekBarThree;
    private int mSpd, mPit, mVol;


    @Nullable
    public View onCreateView(@NonNull LayoutInflater paramLayoutInflater, @Nullable ViewGroup paramViewGroup, @Nullable Bundle paramBundle) {
        View view = View.inflate(this.mActivity, R.layout.fragment_tts, null);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        int spd = SpUtils.getInstance().getInt(Constants.SPD);
        int pit = SpUtils.getInstance().getInt(Constants.PIT);
        int vol = SpUtils.getInstance().getInt(Constants.VOl);
        if (spd == -1) {
            SpUtils.getInstance().putInt(Constants.SPD, 5);
            mSpd = 5;
        } else {
            mSpd = spd;
        }
        if (pit == -1) {
            SpUtils.getInstance().putInt(Constants.PIT, 5);
            mPit = 5;
        } else {
            mPit = pit;
        }

        if (vol == -1) {
            SpUtils.getInstance().putInt(Constants.VOl, 5);
            mVol = 5;
        } else {
            mVol = vol;
        }
        mSeekBarOne.setProgress(mSpd);
        mSeekBarTwo.setProgress(mPit);
        mSeekBarThree.setProgress(mVol);

        String str1 = SpUtils.getInstance().getString(Constants.SERVER_IP_ADDR_PORT_TTS);
        if (TextUtils.isEmpty(str1)) {
            str1 = Constants.SERVER_IP_ADDR_PORT_TTS_DEF;
            SpUtils.getInstance().putString(Constants.SERVER_IP_ADDR_PORT_TTS, Constants.SERVER_IP_ADDR_PORT_TTS_DEF);
        }
        mEtPortIpTTS.setText(str1);
    }

    private void initView(View paramView) {

        mEditText = paramView.findViewById(R.id.mEditText);
        mButton = paramView.findViewById(R.id.mButton);
        mEtPortIpTTS = paramView.findViewById(R.id.mEtPortIpTTS);
        mSeekBarOne = paramView.findViewById(R.id.mSeekBarOne);
        mSeekBarTwo = paramView.findViewById(R.id.mSeekBarTwo);
        mSeekBarThree = paramView.findViewById(R.id.mSeekBarThree);
//        this.mTvTts = ((TextView) paramView.findViewById(R.id.mTvTts));
//
//        this.mTvTts.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//
//                Intent intent = new Intent(TtsFragment.this.mActivity, SettingActivity.class);
//                intent.putExtra("flag", "tts");
//                TtsFragment.this.startActivity(intent);
//            }
//        });
        mSeekBarOne.setOnSeekChangeListener(new MyOnSeekChangeListener() {
            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mSpd = seekBar.getProgress();
                SpUtils.getInstance().putInt(Constants.SPD, mSpd);
            }
        });
        mSeekBarTwo.setOnSeekChangeListener(new MyOnSeekChangeListener() {
            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mPit = seekBar.getProgress();
                SpUtils.getInstance().putInt(Constants.PIT, mPit);
            }
        });
        mSeekBarThree.setOnSeekChangeListener(new MyOnSeekChangeListener() {
            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mVol = seekBar.getProgress();
                SpUtils.getInstance().putInt(Constants.VOl, mVol);
            }
        });
        mEtPortIpTTS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("tag", s + "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("tag", s + "afterTextChanged");
                if (!TextUtils.isEmpty(s)) {
                    SpUtils.getInstance().putString(Constants.SERVER_IP_ADDR_PORT_TTS, s.toString());
                }
            }
        });


        this.mButton.setOnVoiceButtonInterface(new VoiceImgView.onVoiceButtonInterface() {
            public void onStartVoice() {
                TtsFragment.this.getAudio();
            }

            public void onStopVoice() {
                MediaPlayerManeger.getInstance().mDestory();
            }
        });
    }

    private void getAudio() {

        String result = mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this.mActivity, "文字为空", Toast.LENGTH_SHORT).show();
            stopAll();
            return;
        }

        String ipPort = mEtPortIpTTS.getText().toString().trim();
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this.mActivity, "ip地址为空", Toast.LENGTH_SHORT).show();
            stopAll();
            return;
        }

        try {
            RetrofitClient client = new RetrofitClient(ipPort);
            Call<ResponseBody> call = client.getRetrofitService().text2audio(
                    result,
                    "zh",
                    993,
                    1,
                    "fa:16:3c:40:38:4a",
                    mSpd,
                    mPit,
                    mVol,
                    100);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        InputStream inputStream = response.body().byteStream();
                        String audioPath = getAudioPath(inputStream);
                        MediaPlayerManeger.getInstance().mPlay(audioPath);
                        MediaPlayerManeger.getInstance().setMediaPlayerManegerinterface(new MediaPlayerManegerinterface() {
                            @Override
                            public void onComplete() {
                                stopAll();
                            }
                        });
                    } else {
                        Toast.makeText(TtsFragment.this.mActivity, "请求错误", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("tag", t.getMessage());
                    Toast.makeText(TtsFragment.this.mActivity, "请求错误", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "ip地址错误", Toast.LENGTH_SHORT).show();
            stopAll();
        }
    }

    private String getAudioPath(InputStream inputStream) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String ttsPath = path + "/" + System.currentTimeMillis() + ".wav";
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(ttsPath);
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            fs.flush();
            fs.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ttsPath;

    }


    public void stopAll() {
        MediaPlayerManeger.getInstance().mDestory();
        this.mButton.stopAnim();
    }
}

