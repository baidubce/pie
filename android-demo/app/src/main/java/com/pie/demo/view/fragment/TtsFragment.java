package com.pie.demo.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pie.demo.Constants;
import com.pie.demo.R;
import com.pie.demo.base.BaseFragment;
import com.pie.demo.manager.MediaPlayerManeger;
import com.pie.demo.manager.MediaPlayerManegerinterface;
import com.pie.demo.retrofit.RetrofitClient;
import com.pie.demo.utils.SpUtils;
import com.pie.demo.view.activity.SettingActivity;
import com.pie.demo.view.weiget.VoiceImgView;

import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TtsFragment extends BaseFragment {

    private VoiceImgView mButton;
    private EditText mEditText;
    private TextView mTvTts;


    @Nullable
    public View onCreateView(@NonNull LayoutInflater paramLayoutInflater, @Nullable ViewGroup paramViewGroup, @Nullable Bundle paramBundle) {
        View view = View.inflate(this.mActivity, R.layout.fragment_tts, null);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String str1 = SpUtils.getInstance().getString(Constants.SERVER_IP_ADDR_PORT_TTS);
        if (TextUtils.isEmpty(str1)) {
            str1 = Constants.SERVER_IP_ADDR_PORT_TTS_DEF;
            SpUtils.getInstance().putString(Constants.SERVER_IP_ADDR_PORT_TTS, Constants.SERVER_IP_ADDR_PORT_TTS_DEF);
        }
        this.mTvTts.setText(str1);

    }

    private void initView(View paramView) {

        this.mEditText = ((EditText) paramView.findViewById(R.id.mEditText));
        this.mButton = ((VoiceImgView) paramView.findViewById(R.id.mButton));
        this.mTvTts = ((TextView) paramView.findViewById(R.id.mTvTts));

        this.mTvTts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(TtsFragment.this.mActivity, SettingActivity.class);
                intent.putExtra("flag", "tts");
                TtsFragment.this.startActivity(intent);
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

        String str1 = SpUtils.getInstance().getString(Constants.SERVER_IP_ADDR_PORT_TTS);

        RetrofitClient client = new RetrofitClient(str1);
        Call<ResponseBody> call = client.getRetrofitService().text2audio(result, "zh", 993, 1, "fa:16:3c:40:38:4a");
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

