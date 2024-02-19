// Copyright 2020 Baidu Inc. All rights reserved.
// Use of this source code is governed by the license
// that can be found in the LICENSE file.

//go:build darwin
// +build darwin

/*
modification history
--------------------
2020/7/17, by xiashuai01@baidu.com, create
*/

package client

/*
  #include <stdio.h>
  #include <unistd.h>
  #include <termios.h>
  char getch(){
      char ch = 0;
      struct termios old = {0};
      fflush(stdout);
      if( tcgetattr(0, &old) < 0 ) perror("tcsetattr()");
      old.c_lflag &= ~ICANON;
      old.c_lflag &= ~ECHO;
      old.c_cc[VMIN] = 1;
      old.c_cc[VTIME] = 0;
      if( tcsetattr(0, TCSANOW, &old) < 0 ) perror("tcsetattr ICANON");
      if( read(0, &ch,1) < 0 ) perror("read()");
      old.c_lflag |= ICANON;
      old.c_lflag |= ECHO;
      if(tcsetattr(0, TCSADRAIN, &old) < 0) perror("tcsetattr ~ICANON");
      return ch;
  }
*/
import "C"
import (
	"bytes"
	"context"
	b64 "encoding/base64"
	"encoding/binary"
	flagutil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
	"github.com/golang/protobuf/proto"
	"github.com/gordonklaus/portaudio"
	"github.com/zenwerk/go-wave"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"io"
	"log"
)

// 处理麦克风音频流
func ReadMicrophone(headers protogen.InitRequest) {
	conn, err := grpc.Dial(flagutil.ServerAddr, grpc.WithInsecure(), grpc.WithBlock())
	util.ErrorCheck(err)
	defer conn.Close()

	headersBytes, err := proto.Marshal(&headers)
	// 传送metadata
	md := metadata.Pairs("audio_meta", b64.StdEncoding.EncodeToString(headersBytes))
	client := protogen.NewAsrServiceClient(conn)

	ctx := metadata.NewOutgoingContext(context.Background(), md)
	stream, err := client.Send(ctx)
	util.ErrorCheck(err)
	waitc := make(chan struct{})

	go func() {
		for {
			in, err := stream.Recv()
			if err == io.EOF {
				// read done.
				close(waitc)
				return
			}
			util.ErrorCheck(err)
			if in != nil {
				log.Printf(" | start time: %s, end time: %s, result: %s, complete: %t, sn: %s, traceId: %s",
					in.AudioFragment.StartTime, in.AudioFragment.EndTime, in.AudioFragment.Result, in.AudioFragment.Completed, in.AudioFragment.SerialNum, in.TraceId)
				//log.Printf("asr result can get: %s", in.AudioFragment.Result)
			} else {
				log.Printf("asr result error")
			}
		}
	}()

	inputChannels := 1
	outputChannels := 0
	frames16PerBuffer := make([]int16, int(headers.SendPerSeconds*float64(flagutil.SampleRate)))

	portaudio.Initialize()

	portStream, err := portaudio.OpenDefaultStream(inputChannels, outputChannels, float64(flagutil.SampleRate), len(frames16PerBuffer), frames16PerBuffer)
	if err != nil {
		panic(err)
	}

	param := wave.WriterParam{
		Out:           nil,
		Channel:       inputChannels,
		SampleRate:    flagutil.SampleRate,
		BitsPerSample: 16,
	}

	waveWriter, err := wave.NewWriter(param)
	util.ErrorCheck(err)

	err = portStream.Start()
	util.ErrorCheck(err)
	for {
		err = portStream.Read()
		util.ErrorCheck(err)
		_, err := waveWriter.WriteSample16(frames16PerBuffer) // WriteSample16 for 16 bits
		if err != nil {
			panic(err)
		}
		var content = make([][]byte, int(headers.SendPerSeconds*float64(flagutil.SampleRate)*2))
		for _, c := range frames16PerBuffer {
			b := Int16ToBytes(c)
			content = append(content, b)
		}
		audioBytes := bytes.Join(content, []byte(""))

		request := protogen.AudioFragmentRequest{
			AudioData: audioBytes,
		}

		if err := stream.Send(&request); err != nil {
			log.Fatalf("Failed to send a audio stream: %v", err)
		}
	}
	portStream.Close()
	waveWriter.Close()
	stream.CloseSend()
	<-waitc
}

func Int16ToBytes(data int16) []byte {
	bytebuf := bytes.NewBuffer([]byte{})
	binary.Write(bytebuf, binary.LittleEndian, data)
	bytes := bytebuf.Bytes()
	return bytes
}
