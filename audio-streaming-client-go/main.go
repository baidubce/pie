// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

/*
modification history
--------------------
2020/7/17, by xiashuai01@baidu.com, create
*/

package main

import (
	"flag"
	client "github.com/baidubce/pie/audio-streaming-client-go/baiduasr"
	"github.com/baidubce/pie/audio-streaming-client-go/constant"
	flagutil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
	"time"
)

//var (
//	serverAddr = flag.String("server_addr", "127.0.0.1:8051", "The server address in the format of host:port")
//)
//
//var username, password, productId, audioFile string
//var sampleRate int
//var enableFlushData bool

//func init() {
//	flag.StringVar(&username, "username", "123", "The username to login streaming server")
//	flag.StringVar(&password, "password", "123", "The password to login streaming server")
//	flag.StringVar(&productId, "product_id", "1903", "The pid for ASR engine'")
//	flag.IntVar(&sampleRate, "sample_rate", 8000, "The sample rate for ASR engine")
//	flag.StringVar(&audioFile, "audio_file", "testaudio/bj8k.wav", "The audio file path")
//	flag.BoolVar(&enableFlushData, "enable_flush_data", true, "enable flush data")
//}

func generateInitRequest() protogen.InitRequest {

	content := protogen.InitRequest{
		EnableLongSpeech: true,
		EnableChunk:      true,
		EnableFlushData:  flagutil.EnableFlushData,
		ProductId:        flagutil.ProductId,
		SamplePointBytes: 1,
		SendPerSeconds:   0.02,
		SleepRatio:       0,
		AppName:          "go",
		LogLevel:         0,
		UserName:         flagutil.UserName,
	}
	nowTime := time.Now().Format(constant.TIME_FORMAT)
	content.ExpireTime = nowTime
	content.Token = util.HashToken(flagutil.UserName, flagutil.Password, nowTime)
	return content
}

func main() {
	flag.Parse()
	// 处理音频文件
	client.ReadFile(generateInitRequest())
	// 处理麦克风音频流
	//client.ReadMicrophone(generateInitRequest())
}
