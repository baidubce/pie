// Copyright 2023 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

package main

import (
	"time"

	"github.com/baidubce/pie/audio-streaming-client-go/constant"
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
)

// generateInitRequest generateInitRequest
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
