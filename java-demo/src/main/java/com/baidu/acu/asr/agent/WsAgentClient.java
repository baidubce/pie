package com.baidu.acu.agent;

import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import com.baidu.acu.asr.model.AgentArgs;
import com.baidu.acu.util.TimeUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 鉴权信息填写在根目录的Const.java内
 * 参考：<a href='https://github.com/Baidu-AIP/speech_realtime_api'>https://github.com/Baidu-AIP/speech_realtime_api</a>
 * <p>
 * 依赖 org.json:json:20190722 json处理
 * 依赖 com.squareup.okhttp3:okhttp:4.2.1 websocket库
 * 依赖 项目根目录的Const.java及Util.java
 * 测试音频为运行目录下的16k.pcm,日志中有绝对路径
 * <p>
 * STEP 1. 连接
 * STEP 2. 连接成功后发送数据
 * STEP 2.1 发送发送开始参数帧
 * STEP 2.2 实时发送音频数据帧
 * STEP 2.3 库接收识别结果
 * STEP 2.4 发送结束帧
 * STEP 3. 关闭连接
 */
@Slf4j
public class WsAgentClient {
    // 先修改Const里的鉴权参数

    private InputStream inputStream;

    private volatile boolean isClosed = false;
    @Setter
    private volatile AgentArgs agentArgs;

    /**
     * JVM中运行，android studio里会报缺json库
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        AgentArgs agentArgs = AgentArgs.parse(args);
        String filename = agentArgs.getAudioPath();
        // 日志格式和语言
        new WsAgentClient(filename, agentArgs).run();

    }

    private WsAgentClient(String filename, AgentArgs agentArgs) {
        this.agentArgs = agentArgs;
        File file = new File(filename);
        log.info("begin demo, will read " + file.getAbsolutePath());
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
        }
    }

    private WsAgentClient(String filename) {
        File file = new File(filename);
        log.info("begin demo, will read " + file.getAbsolutePath());
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
        }
    }

    public WsAgentClient(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * 发起请求
     */
    public void run() {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3000, TimeUnit.MILLISECONDS).build();
        String url = agentArgs.getUri() + "?sn=" + UUID.randomUUID().toString();
        log.info("runner begin: {}" , url);
        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, new WListener()); // WListener 为回调类
        client.dispatcher().executorService().shutdown();
    }

    /**
     * websocket 是否关闭
     *
     * @return 是否关闭
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * WebSocket 事件回调
     */
    private class WListener extends WebSocketListener {

        /**
         * STEP 2. 连接成功后发送数据
         *
         * @param webSocket WebSocket类
         * @param response  结果
         */
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            // 这里一定不要阻塞
            new Thread(() -> {
                try {
                    // STEP 2.1 发送发送开始参数帧
                    sendStartFrame(webSocket);
                    // STEP 2.2 实时发送音频数据帧
                    sendAudioFrames(webSocket);
                    // STEP 2.4 发送结束帧
                    sendFinishFrame(webSocket);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            // 这里千万别阻塞，包括WebSocketListener其它回调
        }

        /**
         * STEP 2.1 发送发送开始参数帧
         *
         * @param webSocket WebSocket类
         * @throws JSONException Json解析异常
         */
        /*private void sendStartFrame(WebSocket webSocket) throws JSONException {

            JSONObject params = new JSONObject();

            params.put("appid", Const.APPID);
            params.put("appkey", Const.APPKEY);
            params.put("dev_key", Const.APPKEY);

            params.put("dev_pid", Const.DEV_PID);
            params.put("pid", Const.DEV_PID);
            params.put("cuid", "self_defined_server_id_like_mac_address");

            params.put("format", "pcm");
            params.put("sample", 16000);
            params.put("type", 1);
            // 发音人数量
            params.put("role_num", 1);
            // 是否词粒度返回
            params.put("words_piece", true);
            params.put("need_mid", true);
            params.put("need_session_finish", true);

            JSONObject json = new JSONObject();
            json.put("type", "START");
            json.put("data", params);
            log.info("send start FRAME:" + json.toString());
            webSocket.send(json.toString());
        }*/
        private void sendStartFrame(WebSocket webSocket) throws JSONException {

            JSONObject params = new JSONObject();

//            params.put("appid", Const.APPID);
//            params.put("appkey", Const.APPKEY);
            params.put("dev_key", agentArgs.getAppKey());

            params.put("dev_pid", agentArgs.getDevPid());
            params.put("pid", agentArgs.getDevPid());
            params.put("cuid", "self_defined_server_id_like_mac_address");

            params.put("format", "pcm");
            params.put("sample", 16000);
            params.put("type", 1);
            // 发音人数量
            params.put("role_num", 1);
            params.put("vad_mode", 0);
            // 是否词粒度返回
            params.put("words_piece", true);
            params.put("need_mid", true);
            params.put("channels", 1);
            params.put("need_session_finish", true);
            params.put("start_timestamp", System.currentTimeMillis());


            JSONObject json = new JSONObject();
            json.put("type", "START");
            json.put("data", params);
            log.info("send start FRAME:" + json.toString());
            webSocket.send(json.toString());
        }

        /**
         * STEP 2.2 实时发送音频数据帧
         *
         * @param webSocket WebSocket类
         */
        private void sendAudioFrames(WebSocket webSocket) {
            log.info("begin to send DATA frames");
            int bytesPerFrame = TimeUtil.BYTES_PER_FRAME; // 一个帧 160ms的音频数据
            byte[] buffer = new byte[bytesPerFrame];
            int readSize;
            long nextFrameSendTime = System.currentTimeMillis();
            do {
                // 数据帧之间需要有间隔时间， 间隔时间为上一帧的音频长度
                TimeUtil.sleep(5);
//                Util.sleep(nextFrameSendTime - System.currentTimeMillis());
                try {
                    readSize = inputStream.read(buffer);
                } catch (IOException | RuntimeException e) {
                    log.warn("inputstream is closed:" + e.getClass().getSimpleName() + ":" + e.getMessage());
                    readSize = -2;
                }
                if (readSize > 0) { // readSize = -1 代表流结束
                    ByteString bytesToSend = ByteString.of(buffer, 0, readSize);
                    nextFrameSendTime = System.currentTimeMillis() + TimeUtil.bytesToTime(readSize);
                    log.info("should wait to send next DATA Frame: " + TimeUtil.bytesToTime(readSize) + "ms | send binary bytes size :" + readSize);
                    webSocket.send(bytesToSend);
                }
            } while (readSize >= 0);
        }

        /**
         * STEP 2.4 发送结束帧
         *
         * @param webSocket WebSocket 类
         * @throws JSONException Json解析错误
         */
        private void sendFinishFrame(WebSocket webSocket) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("type", "FINISH");
            log.info("send FINISH FRAME:" + json.toString());
            webSocket.send(json.toString());
        }

        /**
         * STEP 2.3 库接收识别结果
         *
         * @param webSocket WebSocket 类
         * @param text      返回的json
         */
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            // 这里千万别阻塞，包括WebSocketListener其它回调
            if (text.contains("\"TYPE_HEARTBEAT\"")) {
                log.info("receive heartbeat: " + text.trim());
            } else {
                log.info("receive text: " + text.trim());
            }
        }

        /**
         * STEP 3. 关闭连接
         * 服务端关闭连接事件
         *
         * @param webSocket WebSocket 类
         * @param code      状态码
         * @param reason    状态描述
         */
        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
            // 收到服务端关闭
            // 需要停止发任何数据，为了简单，这个demo里遇见报错后没有这段逻辑，具体运行full.Main查看
            log.info("websocket event closing :" + code + " | " + reason);
            // 客户端关闭
            webSocket.close(1000, "");
        }

        /**
         * 客户端关闭回调
         *
         * @param webSocket WebSocket 类
         * @param code      状态码
         * @param reason    状态描述
         */
        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            log.info("websocket closed: " + code + " | " + reason);
            isClosed = true;
        }


        /**
         * 库自身的报错，如断网
         *
         * @param webSocket WebSocket 类
         * @param t         异常
         * @param response  返回
         */
        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            // 这里千万别阻塞，包括WebSocketListener其它回调
            isClosed = true;
        }
    }

}
