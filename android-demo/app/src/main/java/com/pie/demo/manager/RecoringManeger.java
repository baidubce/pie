package com.pie.demo.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.acu.pie.AudioStreaming;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.AsrProduct;
import com.baidu.acu.pie.model.RecognitionResult;
import com.google.protobuf.ByteString;
import com.pie.demo.Constants;
import com.pie.demo.utils.SpUtils;

import java.util.concurrent.CountDownLatch;

import io.grpc.stub.StreamObserver;

public class RecoringManeger {

    //44100、22050、11025，4000、8000。
    private static final int SAMPLERATEINHZ = 8000;
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

    }

    /**
     * 停止录音
     */

    public void stopRecord() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                isRecord = false;
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
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
            }
        }).start();
    }

    /**
     * 初始化AudioRecord
     */

    private void initAudioRecord() {


        String hz = SpUtils.getInstance().getString(Constants.SAMPLERATEINHZ);

        if (TextUtils.isEmpty(hz)) {
            MINBUFFERSIZE = AudioRecord.getMinBufferSize(SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT) * 25;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATEINHZ, CHANNELCONFIG, AUDIOFORMAT, MINBUFFERSIZE);
        } else {
            MINBUFFERSIZE = AudioRecord.getMinBufferSize(Integer.parseInt(hz), CHANNELCONFIG, AUDIOFORMAT) * 25;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Integer.parseInt(hz), CHANNELCONFIG, AUDIOFORMAT, MINBUFFERSIZE);
        }
    }

    private void writeData() {

        HavaThread = 0;

        /**
         * 初始化AsrClientGrpcImpl
         */
        initSp();

        byte[] audiodata = new byte[MINBUFFERSIZE];

        while (isRecord) {
            int readSize = audioRecord.read(audiodata, 0, MINBUFFERSIZE);
            //  -3(可能录音被禁止)
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                try {
                    AudioStreaming.AudioFragmentRequest request = AudioStreaming.AudioFragmentRequest.newBuilder()
                            .setAudioData(ByteString.copyFrom(audiodata))
                            .build();
                    if (streamObserverOne != null) {
                        streamObserverOne.onNext(request);
                    }
                    if (streamObserverTwo != null) {
                        streamObserverTwo.onNext(request);
                    }
                    if (streamObserverThree != null) {
                        streamObserverThree.onNext(request);
                    }
                } catch (Exception e) {
                    if (streamObserverOne != null) {
                        streamObserverOne.onError(e);
                    }
                    if (streamObserverTwo != null) {
                        streamObserverTwo.onError(e);
                    }
                    if (streamObserverThree != null) {
                        streamObserverThree.onError(e);
                    }
                }
            }
        }
    }

    private void initSp() {
        String oneAddress = SpUtils.getInstance().getString(Constants.ONEADDRESS);
        String onePort = SpUtils.getInstance().getString(Constants.ONEPORT);
        int oneAsr = SpUtils.getInstance().getInt(Constants.ONEASRPRODUCT);

        if (!TextUtils.isEmpty(oneAddress) && !TextUtils.isEmpty(onePort) && oneAsr != -1) {
            HavaThread++;
            Log.e("tag", values[oneAsr] + "");
            AsrConfig config = new AsrConfig();
            config.serverIp(oneAddress)
                    .serverPort(Integer.parseInt(onePort))
                    .appName("android")
                    .product(values[oneAsr]);

            try {
                asrClientGrpcOne = new AsrClientGrpcImpl(config);
                streamObserverOne = asrClientGrpcOne.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {
                        int errorCode = recognitionResult.getErrorCode();
                        if (0 != errorCode) {
                            String errorMessage = recognitionResult.getErrorMessage();
                            if (recoringManaegerInterfaceOne != null) {
                                recoringManaegerInterfaceOne.onError(errorMessage);
                            }
                        } else {
                            String result = recognitionResult.getResult();
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceOne != null) {
                                recoringManaegerInterfaceOne.onNext(result, completed);
                            }
                        }
                    }
                }, new CountDownLatch(1));
            } catch (Exception e) {
                if (recoringManaegerInterfaceOne != null) {
                    recoringManaegerInterfaceOne.onError(e.getMessage());
                }
            }
        }

        String twoAddress = SpUtils.getInstance().getString(Constants.TWOADDRESS);
        String twoPort = SpUtils.getInstance().getString(Constants.TWOPORT);
        int twoAsr = SpUtils.getInstance().getInt(Constants.TWOASRPRODUCT);

        if (!TextUtils.isEmpty(twoAddress) && !TextUtils.isEmpty(twoPort) && twoAsr != -1) {
            HavaThread++;
            AsrConfig config = new AsrConfig();
            config.serverIp(twoAddress)
                    .serverPort(Integer.parseInt(twoPort))
                    .appName("android")
                    .product(values[twoAsr]);

            try {
                asrClientGrpcTwo = new AsrClientGrpcImpl(config);
                streamObserverTwo = asrClientGrpcTwo.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {
                        int errorCode = recognitionResult.getErrorCode();
                        if (0 != errorCode) {
                            String errorMessage = recognitionResult.getErrorMessage();
                            if (recoringManaegerInterfaceTwo != null) {
                                recoringManaegerInterfaceTwo.onError(errorMessage);
                            }
                        } else {
                            String result = recognitionResult.getResult();
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceTwo != null) {
                                recoringManaegerInterfaceTwo.onNext(result, completed);
                            }
                        }
                    }
                }, new CountDownLatch(1));
            } catch (Exception e) {
                if (recoringManaegerInterfaceTwo != null) {
                    recoringManaegerInterfaceTwo.onError(e.getMessage());
                }
            }
        }

        String threeAddress = SpUtils.getInstance().getString(Constants.THREEADDRESS);
        String threePort = SpUtils.getInstance().getString(Constants.THREEPORT);
        int threeAsr = SpUtils.getInstance().getInt(Constants.THREEASRPRODUCT);

        if (!TextUtils.isEmpty(threeAddress) && !TextUtils.isEmpty(threePort) && threeAsr != -1) {
            HavaThread++;
            AsrConfig config = new AsrConfig();
            config.serverIp(threeAddress)
                    .serverPort(Integer.parseInt(threePort))
                    .appName("android")
                    .product(values[threeAsr]);

            try {
                asrClientGrpcThree = new AsrClientGrpcImpl(config);
                streamObserverThree = asrClientGrpcThree.asyncRecognize(new Consumer<RecognitionResult>() {
                    @Override
                    public void accept(RecognitionResult recognitionResult) {
                        int errorCode = recognitionResult.getErrorCode();
                        if (0 != errorCode) {
                            String errorMessage = recognitionResult.getErrorMessage();
                            if (recoringManaegerInterfaceThree != null) {
                                recoringManaegerInterfaceThree.onError(errorMessage);
                            }
                        } else {
                            String result = recognitionResult.getResult();
                            boolean completed = recognitionResult.isCompleted();
                            if (recoringManaegerInterfaceThree != null) {
                                recoringManaegerInterfaceThree.onNext(result, completed);
                            }
                        }
                    }
                }, new CountDownLatch(1));
            } catch (Exception e) {
                if (recoringManaegerInterfaceThree != null) {
                    recoringManaegerInterfaceThree.onError(e.getMessage());
                }
            }
        }
    }

    private AsrClientGrpcImpl asrClientGrpcOne = null;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> streamObserverOne;
    private RecoringManaegerInterfaceOne recoringManaegerInterfaceOne = null;

    public void setRecoringManaegerInterfaceOne(RecoringManaegerInterfaceOne recoringManaegerInterfaceOne) {
        this.recoringManaegerInterfaceOne = recoringManaegerInterfaceOne;
    }

    private AsrClientGrpcImpl asrClientGrpcTwo = null;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> streamObserverTwo;
    private RecoringManaegerInterfaceTwo recoringManaegerInterfaceTwo = null;

    public void setRecoringManaegerInterfaceTwo(RecoringManaegerInterfaceTwo recoringManaegerInterfaceTwo) {
        this.recoringManaegerInterfaceTwo = recoringManaegerInterfaceTwo;
    }

    private AsrClientGrpcImpl asrClientGrpcThree = null;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> streamObserverThree;
    private RecoringManaegerInterfaceThree recoringManaegerInterfaceThree = null;

    public void setRecoringManaegerInterfaceThree(RecoringManaegerInterfaceThree recoringManaegerInterfaceThree) {
        this.recoringManaegerInterfaceThree = recoringManaegerInterfaceThree;
    }
}
