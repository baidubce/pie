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
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
	"strings"
	"time"
)

func generateInitRequest() protogen.InitRequest {

	content := protogen.InitRequest{
		EnableLongSpeech: true,
		EnableChunk:      true,
		EnableFlushData:  flagUtil.EnableFlushData,
		ProductId:        flagUtil.ProductId,
		SamplePointBytes: 1,
		SendPerSeconds:   0.02,
		SleepRatio:       flagUtil.SleepRatio,
		AppName:          "go",
		LogLevel:         0,
		UserName:         flagUtil.UserName,
	}
	nowTime := time.Now().Format(constant.TimeFormat)
	content.ExpireTime = nowTime
	content.Token = util.HashToken(flagUtil.UserName, flagUtil.Password, nowTime)
	return content
}

func main() {
	// go run main.go --server_addr 127.0.0.1:8051 --username username --password password --audio_file /path/of/audio.wav
	flag.Parse()
	runType := strings.ToLower(flagUtil.RunType)

	if runType == "file" {
		// 处理音频文件
		client.ReadFile(generateInitRequest())
	}

	if runType == "microphone" {
		// 处理麦克风音频流
		client.ReadMicrophone(generateInitRequest())
	}
}
