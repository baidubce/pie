package com.baidu.acu.asr.model;

import lombok.Data;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 类<code>TtsArgs</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 * @date 2023-08-17
 **/
@Data
public class TtsArgs {
    @Option(name = "-ip", usage = "tts server ip")
    private String ip = "127.0.0.1";
    @Option(name = "-port", usage = "tts server port")
    private Integer port = 8081;

    @Option(name = "-per", usage = "tts product id")
    private Integer per = 5106;
    @Option(name = "-aue", usage = "aue")
    private Integer aue = 4;
    @Option(name = "-vol", usage = "vol")
    private Integer vol = 5;
    @Option(name = "-lan", usage = "lan")
    private String lan = "zh";
    @Option(name = "-pit")
    private Integer pit = 5;
    @Option(name = "-spd", usage = "tts spd")
    private Integer spd = 5;
    @Option(name = "-xml", usage = "tts xml")
    private Integer xml = 0;
    @Option(name = "-name", usage = "post processing name")
    private String name = "";
    @Option(name = "-text", required = true, usage = "tts text")
    private String text = "";
    @Option(name = "-file-path", usage = "tts file path")
    private String filePath = "audio.pcm";
    @Option(name = "-type", usage = "tts type: read or save")
    private String type = "save";

    public static TtsArgs parse(String[] args) {
        TtsArgs iArgs = new TtsArgs();
        CmdLineParser parser = new CmdLineParser(iArgs);
        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return iArgs;
    }
}
