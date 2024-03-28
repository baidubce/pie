// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

/*
modification history
--------------------
2020/7/17, by xiashuai01@baidu.com, create
*/

// +build darwin
package main

import (
	"flag"
	"strings"

	client "github.com/baidubce/pie/audio-streaming-client-go/baiduasr"
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
)

// go build -o audio-streaming-client-go main_darwin.go
func main() {
	// go run main.go --server_addr www.example-asr.com:8051 --username username --password password --audio_file /path/of/audio.wav --ssl_path ca/server.crt
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
