// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

//go:build linux
// +build linux

/*
modification history
--------------------
2020/7/17, by xiashuai01@baidu.com, create
*/

package main

import (
	"flag"
	"strings"

	client "github.com/baidubce/pie/audio-streaming-client-go/baiduasr"
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
)

// GOOS=linux GOARCH=arm64 go build
func main() {
	// go run main.go --server_addr www.example-asr.com:8051 --username username --password password --audio_file /path/of/audio.wav --ssl_path ca/server.crt
	flag.Parse()
	runType := strings.ToLower(flagUtil.RunType)

	if runType == "file" {
		// 处理音频文件
		client.ReadFile(util.GenerateInitRequest())
	}
}
