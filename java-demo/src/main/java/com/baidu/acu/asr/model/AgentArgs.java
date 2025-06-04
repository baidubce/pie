package com.baidu.acu.asr.model;

import lombok.Data;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 类<code>AgentArgs</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2024-04-24
 **/
@Data
public class AgentArgs {

    @Option(name = "-appid", required = false, usage = "asr appid")
    private int appId = 32388532;
    @Option(name = "-app-key", required = false, usage = "asr appkey")
    private String appKey = "com.baidu.open";
    /* dev_pid 是语言模型 ， 可以修改为其它语言模型测试，如远场普通话 19362*/
    @Option(name = "-dev-pid", required = false, usage = "asr dev pid")
    private int devPid = 1912;
    @Option(name = "-uri", usage = "asr uri")
    String uri = "ws://182.61.62.45:443/realtime_asr";
    @Option(name = "-audio-path", usage = "audio save path")
    private String audioPath = "xeq16k.wav";
    @Option(name = "-format", usage = "audio format")
    private String format = "pcm";
    @Option(name = "-role-num", usage = "role num")
    private int roleNum = 1;
    @Option(name = "-vad-type", usage = "vad type")
    private int vadType = 1;
    @Option(name = "-vad-mode", usage = "vad mode")
    private int vadMode = 0;
    @Option(name = "-channels", usage = "audio channels")
    private int channels = 1;
    @Option(name = "-sample-rate", usage = "audio sample rate")
    private int sampleRate = 16000;
    @Option(name = "-type", usage = "server type, file or microphone, default: file")
    private String type = "file";
    @Option(name = "-username", usage = "username")
    private String username = "username";
    @Option(name = "-password", usage = "password")
    private String password = "password";

    public static AgentArgs parse(String[] args) {
        AgentArgs iAgentArgs = new AgentArgs();
        CmdLineParser parser = new CmdLineParser(iAgentArgs);
        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return iAgentArgs;
    }
}
