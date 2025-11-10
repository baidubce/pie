package com.baidu.acu.asr.model;

import com.baidu.acu.pie.model.AsrProduct;
import lombok.Data;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

/**
 * Args
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2021/3/3 2:41 下午
 */
@Data
public class WebProxyArgs {
    @Option(name = "-uri", usage = "asr uri")
    String uri = "ws://ai-speech.baidu.com/api/v1/asr/stream";
    @Option(name = "-pid", usage = "asr product id")
    private String productId = "1912";
    @Option(name = "-username", usage = "username")
    private String username = "username";
    @Option(name = "-password", usage = "password")
    private String password = "***";
    @Option(name = "-enable-flush-data", handler = BooleanOptionHandler.class, usage = "enable flush data")
    private Boolean enableFlushData = false;
    @Option(name = "-audio-path", usage = "audio save path")
    private String audioPath = "testaudio/xeq16k.wav";
    // 这个只针对speex
    @Option(name = "-times", usage = "use times")
    private int times = 10;
    @Option(name = "-sleep-time", usage = "sleep time, unit ms")
    private int sleepTime = 10;
    @Option(name = "-sleep-ratio", usage = "asr sleep ratio")
    private double sleepRatio = 0.3;


    public static WebProxyArgs parse(String[] args) {
        WebProxyArgs iAsrArgs = new WebProxyArgs();
        CmdLineParser parser = new CmdLineParser(iAsrArgs);
        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return iAsrArgs;
    }

    public static AsrProduct parseProduct(String pid) {

        return new AsrProduct(pid, 16000);
    }
}
