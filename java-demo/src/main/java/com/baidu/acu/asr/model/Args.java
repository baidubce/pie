package com.baidu.acu.asr.model;

import com.baidu.acu.pie.model.AsrProduct;
import lombok.Data;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Args
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2021/3/3 2:41 下午
 */
@Data
public class Args {
    @Option(name = "-ip", required = true, usage = "asr server ip")
    private String ip = "127.0.0.1";
    @Option(name = "-port", required = true, usage = "asr server port")
    private int port = 8051;
    @Option(name = "-pid", required = true, usage = "asr product id")
    private String productId = "1912";
    @Option(name = "-username", required = true, usage = "username")
    private String username = "";
    @Option(name = "-password", required = true, usage = "password")
    private String password = "";
    @Option(name = "-audio-path", usage = "audio save path")
    private String audioPath = "";

    public static Args parse(String[] args) {
        Args iArgs = new Args();
        CmdLineParser parser = new CmdLineParser(iArgs);
        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
        return iArgs;
    }

    public AsrProduct parseProduct(String productId) {
        for (AsrProduct product : AsrProduct.values()) {
            if (product.getCode().equals(productId)) {
                return product;
            }
        }
        return null;
    }
}
