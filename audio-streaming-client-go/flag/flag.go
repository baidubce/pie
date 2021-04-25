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
	SleepRatio      float64

	RunType string
)

func init() {
	flag.StringVar(&ServerAddr, "server_addr", "127.0.0.1:8051", "The server address in the format of host:port")
	flag.StringVar(&UserName, "username", "123", "The UserName to login streaming server")
	flag.StringVar(&Password, "password", "123", "The Password to login streaming server")
	flag.StringVar(&ProductId, "product_id", "1912", "The pid for ASR engine")
	flag.IntVar(&SampleRate, "sample_rate", 16000, "The sample rate for ASR engine")
	flag.StringVar(&AudioFile, "audio_file", "audio/bj8k.wav", "The audio file path")
	flag.BoolVar(&EnableFlushData, "enable_flush_data", true, "enable flush data")
	flag.Float64Var(&SleepRatio, "sleep_ratio", 1, "sleep ratio")

	flag.StringVar(&RunType, "run_type", "file", "run type, must in [file, microphone], default file")
}
