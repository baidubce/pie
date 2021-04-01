package client

import (
	"context"
	b64 "encoding/base64"
	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
	"github.com/golang/protobuf/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"io"
	"log"
	"os"
	"time"
)

// 处理音频文件音频流
func ReadFile(headers protogen.InitRequest) {
	conn, err := grpc.Dial(flagUtil.ServerAddr, grpc.WithInsecure(), grpc.WithBlock())
	util.ErrorCheck(err)
	defer conn.Close()

	bytes, err := proto.Marshal(&headers)
	// 传送metadata
	md := metadata.Pairs("audio_meta", b64.StdEncoding.EncodeToString(bytes))

	client := protogen.NewAsrServiceClient(conn)

	ctx := metadata.NewOutgoingContext(context.Background(), md)
	stream, err := client.Send(ctx)
	util.ErrorCheck(err)
	waitC := make(chan struct{})

	go func() {
		for {
			in, err := stream.Recv()
			if err == io.EOF {
				// read done.
				close(waitC)
				return
			}
			util.ErrorCheck(err)
			if in != nil {
				log.Printf("asr result can get: %s", in.AudioFragment.Result)
			} else {
				log.Printf("asr result error")
			}
		}
	}()

	audioFileStream, err := os.Open(flagUtil.AudioFile)
	util.ErrorCheck(err)
	defer audioFileStream.Close()

	sendPackageSize := int(headers.SendPerSeconds*float64(flagUtil.SampleRate)*2) * 2
	audioBytes := make([]byte, sendPackageSize)
	for {
		_, err := audioFileStream.Read(audioBytes)
		if err == io.EOF {
			log.Printf("send finish")
			break
		}

		request := protogen.AudioFragmentRequest{
			AudioData: audioBytes,
		}

		if err := stream.Send(&request); err != nil {
			log.Fatalf("Failed to send a audio stream: %v", err)
		}

		if flagUtil.SleepRatio != 0 {
			time.Sleep(time.Duration(20/flagUtil.SleepRatio) * time.Millisecond)
		}
	}

	stream.CloseSend()
	<-waitC
}
