// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

/*
modification history
--------------------
2020/7/20, by xiashuai01@baidu.com, create
*/

package flag

import "flag"

var (
	ServerAddr      string
	UserName        string
	Password        string
	ProductId       string
	AudioFile       string
	SampleRate      int
	EnableFlushData bool
)

func init() {
	flag.StringVar(&ServerAddr, "server_addr", "127.0.0.1:8051", "The server address in the format of host:port")
	flag.StringVar(&UserName, "username", "123", "The UserName to login streaming server")
	flag.StringVar(&Password, "password", "123", "The Password to login streaming server")
	flag.StringVar(&ProductId, "product_id", "1903", "The pid for ASR engine'")
	flag.IntVar(&SampleRate, "sample_rate", 8000, "The sample rate for ASR engine")
	flag.StringVar(&AudioFile, "audio_file", "testaudio/bj8k.wav", "The audio file path")
	flag.BoolVar(&EnableFlushData, "enable_flush_data", true, "enable flush data")
}
