package main

import (
	"flag"
	"strings"

	client "github.com/baidubce/pie/audio-streaming-client-go/baiduasr"
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
)

// go build -o audio-streaming-client-go main_darwin.go
func main() {
	// go run main.go --server_addr www.example-asr.com:8051 --username username --password password --audio_file /path/of/audio.wav --ssl_path ca/server.crt
	flag.Parse()
	runType := strings.ToLower(flagUtil.RunType)

	if runType == "microphone" {
		// 处理音频文件
		client.ReadFile(util.GenerateInitRequest())
	}
}
