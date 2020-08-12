package com.pie.demo.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.acu.pie.AudioStreaming;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.ChannelConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.util.JacksonUtil;
import com.google.protobuf.ByteString;
import com.pie.demo.Constants;
import com.pie.demo.utils.SpUtils;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class RecoringManeger {


    //44100、22050、11025，4000、8000。
    private int SAMPLERATEINHZ = 8000;
    //MONO单声道，STEREO立体声
    private static final int CHANNELCONFIG = AudioFormat.CHANNEL_IN_MONO;
    //采样大小16bit 或者8bit。
    private static final int AUDIOFORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // 采集数据缓冲区的大小
    private int MINBUFFERSIZE = 0;

    private boolean isRecord = false;
    private final AsrProduct[] values;

    public boolean isRecord() {
        return isRecord;
    }

    private AudioRecord audioRecord = null;

    private int HavaThread = 0;

    public int getHavaThread() {
        return HavaThread;
    }

    private static RecoringManeger instance;

    public static RecoringManeger getInstance() {
        if (null == instance) {
            synchronized (RecoringManeger.class) {
                if (null == instance) {
                    instance = new RecoringManeger();
                }
            }
        }
        return instance;
    }

    private RecoringManeger() {
        values = AsrProduct.values();
    }

    /**
     * 开始录音
     */
    public void startRecord() {

        try {

            if (audioRecord == null) {
                initAudioRecord();
            }


            isRecord = true;
            audioRecord.startRecording();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    writeData();
                }
            }).start();

        } catch (Exception e) {
            Log.e("tag", "error " + e.getMessage());
        }

    }

    /**
     * 停止录音
     */

    public void stopRecord() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isRecord = false;
                    if (audioRecord != null) {
                        audioRecord.stop();
                        audioRecord.release();
                        audioRecord = null;
                    }


                    if (streamObserverOne != null) {
                        streamObserverOne.getFinishLatch().await();
                        streamObserverOne.complete();
                    }
                    if (streamObserverTwo != null) {
                        streamObserverTwo.complete();
                        streamObserverTwo.getFinishLatch().await();
                    }
                    if (streamObserverThree != null) {
                        streamObserverThree.complete();
                        streamObserverThree.getFinishLatch().await();
                    }
                    if (asrClientGrpcOne != null) {
                        asrClientGrpcOne.shutdown();
                    }
                    if (asrClientGrpcTwo != null) {
                        asrClientGrpcTwo.shutdown();
                    }
                    if (asrClientGrpcThree != null) {
                        asrClientGrpcThree.shutdown();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 初始化AudioRecord
     */

    private void initAudioRecord() {


//        String hz = SpUtils.getInstance().getString(Constants.SAMPLERATEINHZ);
        int oneAsr = SpUtils.getInstance().getInt(Constants.ONEASRPRODUCT);
        AsrProduct value = values[oneAsr];
        if (value == AsrProduct.INPUT_METHOD || value == AsrProduct.FAR_FIELD || value == AsrProduct.FAR_FIELD_ROBOT || value == AsrProduct.SPEECH_SERVICE) {
            SAMPLERATEINHZ = 16000;
        } else {
            SAMPLERATEINHZ = 8000;
        }

        Log.e("tag", SAMPLERATEINHZ + "");

        MINBUFFERSIZE = AudioRecord.getMinBufferSize(SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT) * 25;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT, MINBUFFERSIZE);

//        if (TextUtils.isEmpty(hz)) {
//            MINBUFFERSIZE = AudioRecord.getMinBufferSize(SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT) * 25;
//            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT, MINBUFFERSIZE);
//        } else {
//            MINBUFFERSIZE = AudioRecord.getMinBufferSize(Integer.parseInt(hz), CHANNELCONFIG, AUDIOFORMAT) * 25;
//            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Integer.parseInt(hz), CHANNELCONFIG, AUDIOFORMAT, MINBUFFERSIZE);
//        }
    }

    private void writeData() {

        HavaThread = 0;

        /**
         * 初始化AsrClientGrpcImpl
         */
        initSp();

        byte[] audiodata = new byte[MINBUFFERSIZE];

        try {
            while (isRecord) {
                int readSize = audioRecord.read(audiodata, 0, MINBUFFERSIZE);
                //  -3(可能录音被禁止)
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {

                    AudioStreaming.AudioFragmentRequest request = AudioStreaming.AudioFragmentRequest.newBuilder()
                            .setAudioData(ByteString.copyFrom(audiodata))
                            .build();
                    if (streamObserverOne != null) {
//                        streamObserverOne.getSender().onNext(request);
                        streamObserverOne.send(audiodata);
                    }
                    if (streamObserverTwo != null) {
//                        streamObserverTwo.getSender().onNext(request);
                        streamObserverTwo.send(audiodata);
                    }
                    if (streamObserverThree != null) {
//                        streamObserverThree.getSender().onNext(request);
                        streamObserverThree.send(audiodata);
                    }
                }
            }
        } catch (Exception e) {
            if (streamObserverOne != null) {
                streamObserverOne.getSender().onError(e);
            }
            if (streamObserverTwo != null) {
                streamObserverTwo.getSender().onError(e);
            }
            if (streamObserverThree != null) {
                streamObserverThree.getSender().onError(e);
            }
        }
    }

    /**
     * 设置常用的控制语音识别的参数
     */
    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(0);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);
        // 随路信息根据需要设置
        Map<String, Object> extra_info = new HashMap<>();
        extra_info.put("demo", "java");
        requestMetaData.setExtraInfo(JacksonUtil.objectToString(extra_info));

        return requestMetaData;
    }

    private void initSp() {
        String oneAddress = SpUtils.getInstance().getString(Constants.ONEADDRESS);
        String onePort = SpUtils.getInstance().getString(Constants.ONEPORT);
        int oneAsr = SpUtils.getInstance().getInt(Constants.ONEASRPRODUCT);

        String oneAccout = SpUtils.getInstance().getString(Constants.ACCOUTONE);
        String onePwd = SpUtils.getInstance().getString(Constants.PWDONE);
        String oneTime = SpUtils.getInstance().getString(Constants.TIMEONE);
        String oneToken = SpUtils.getInstance().getString(Constants.TOKENONE);


        if (!TextUtils.isEmpty(oneAddress) && !TextUtils.isEmpty(onePort) && oneAsr != -1) {
            HavaThread++;
            Log.e("tag", values[oneAsr] + "xxx");
            AsrConfig config = new AsrConfig();
            config.serverIp(oneAddress)
                    .serverPort(Integer.parseInt(onePort))
                    .appName("android")
                    .product(values[oneAsr])
                    .userName(oneAccout)
                    .password(onePwd)
                    .token(oneToken);
            if (!TextUtils.isEmpty(oneTime)) {
                config.expireDateTime(DateTime.parse(oneTime));
            }

            try {
                asrClientGrpcOne = new AsrClientGrpcImpl(config);
                streamObserverOne = asrClientGrpcOne.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {

                        String result = recognitionResult.getResult();
                        Log.e("tag", "res:" + result);
                        if (TextUtils.isEmpty(result)) {
                            Log.e("tag", "error");
                            if (recoringManaegerInterfaceOne != null) {
                                recoringManaegerInterfaceOne.onError("err");
                            }
                        } else {
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceOne != null) {
                                recoringManaegerInterfaceOne.onNext(result, completed);
                            }
                        }

                    }
                }, createRequestMeta());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("tag", "catch:");
                if (recoringManaegerInterfaceOne != null) {
                    recoringManaegerInterfaceOne.onError(e.getMessage());
                }
            }
        }


        String twoAccout = SpUtils.getInstance().getString(Constants.ACCOUTTWO);
        String twoPwd = SpUtils.getInstance().getString(Constants.PWDTWO);
        String twoTime = SpUtils.getInstance().getString(Constants.TIMETWO);
        String twoToken = SpUtils.getInstance().getString(Constants.TOKENTWO);


        String twoAddress = SpUtils.getInstance().getString(Constants.TWOADDRESS);
        String twoPort = SpUtils.getInstance().getString(Constants.TWOPORT);
        int twoAsr = SpUtils.getInstance().getInt(Constants.TWOASRPRODUCT);

        if (!TextUtils.isEmpty(twoAddress) && !TextUtils.isEmpty(twoPort) && twoAsr != -1) {
            HavaThread++;
            AsrConfig config = new AsrConfig();
            config.serverIp(twoAddress)
                    .serverPort(Integer.parseInt(twoPort))
                    .appName("android")
                    .product(values[oneAsr])
                    .userName(twoAccout)
                    .password(twoPwd)
                    .token(twoToken);

            if (!TextUtils.isEmpty(twoTime)) {
                config.expireDateTime(DateTime.parse(twoTime));
            }

            try {
                asrClientGrpcTwo = new AsrClientGrpcImpl(config);
                streamObserverTwo = asrClientGrpcTwo.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {

                        String result = recognitionResult.getResult();
                        Log.e("tag", "res:" + result);
                        if (TextUtils.isEmpty(result)) {
                            if (recoringManaegerInterfaceTwo != null) {
                                recoringManaegerInterfaceTwo.onError("err");
                            }
                        } else {
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceTwo != null) {
                                recoringManaegerInterfaceTwo.onNext(result, completed);
                            }
                        }
                    }
                }, createRequestMeta());
            } catch (Exception e) {
                if (recoringManaegerInterfaceTwo != null) {
                    recoringManaegerInterfaceTwo.onError(e.getMessage());
                }
            }
        }

        String threeAccout = SpUtils.getInstance().getString(Constants.ACCOUTHREE);
        String threePwd = SpUtils.getInstance().getString(Constants.PWDTHREE);
        String threeTime = SpUtils.getInstance().getString(Constants.TIMETHREE);
        String threeToken = SpUtils.getInstance().getString(Constants.TOKENTHREE);


        String threeAddress = SpUtils.getInstance().getString(Constants.THREEADDRESS);
        String threePort = SpUtils.getInstance().getString(Constants.THREEPORT);
        int threeAsr = SpUtils.getInstance().getInt(Constants.THREEASRPRODUCT);

        if (!TextUtils.isEmpty(threeAddress) && !TextUtils.isEmpty(threePort) && threeAsr != -1) {
            HavaThread++;
            AsrConfig config = new AsrConfig();
            config.serverIp(threeAddress)
                    .serverPort(Integer.parseInt(threePort))
                    .appName("android")
                    .product(values[oneAsr])
                    .userName(threeAccout)
                    .password(threePwd)
                    .token(threeToken);

            if (!TextUtils.isEmpty(threeTime)) {
                config.expireDateTime(DateTime.parse(threeTime));
            }


            try {
                asrClientGrpcThree = new AsrClientGrpcImpl(config);
                streamObserverThree = asrClientGrpcThree.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {

                        String result = recognitionResult.getResult();
                        Log.e("tag", "res:" + result);
                        if (TextUtils.isEmpty(result)) {
                            if (recoringManaegerInterfaceThree != null) {
                                recoringManaegerInterfaceThree.onError("err");
                            }
                        } else {
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceThree != null) {
                                recoringManaegerInterfaceThree.onNext(result, completed);
                            }
                        }
                    }
                }, createRequestMeta());
            } catch (Exception e) {
                if (recoringManaegerInterfaceThree != null) {
                    recoringManaegerInterfaceThree.onError(e.getMessage());
                }
            }
        }
    }

    private AsrClientGrpcImpl asrClientGrpcOne = null;
    private StreamContext streamObserverOne;
    private RecoringManaegerInterfaceOne recoringManaegerInterfaceOne = null;

    public void setRecoringManaegerInterfaceOne(RecoringManaegerInterfaceOne recoringManaegerInterfaceOne) {
        this.recoringManaegerInterfaceOne = recoringManaegerInterfaceOne;
    }

    private AsrClientGrpcImpl asrClientGrpcTwo = null;
    private StreamContext streamObserverTwo;
    private RecoringManaegerInterfaceTwo recoringManaegerInterfaceTwo = null;

    public void setRecoringManaegerInterfaceTwo(RecoringManaegerInterfaceTwo recoringManaegerInterfaceTwo) {
        this.recoringManaegerInterfaceTwo = recoringManaegerInterfaceTwo;
    }

    private AsrClientGrpcImpl asrClientGrpcThree = null;
    private StreamContext streamObserverThree;
    private RecoringManaegerInterfaceThree recoringManaegerInterfaceThree = null;

    public void setRecoringManaegerInterfaceThree(RecoringManaegerInterfaceThree recoringManaegerInterfaceThree) {
        this.recoringManaegerInterfaceThree = recoringManaegerInterfaceThree;
    }
}
