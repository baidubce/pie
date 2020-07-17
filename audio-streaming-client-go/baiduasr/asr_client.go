package client

import (
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"

	"bufio"
	"context"
	b64 "encoding/base64"
	"flag"
	"github.com/golang/protobuf/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"io"
	"log"
	"os"
)

// 处理音频文件音频流
func ReadFile(serverAddr, audioFile string, sampleRate int, headers protogen.InitRequest) {
	conn, err := grpc.Dial(serverAddr, grpc.WithInsecure(), grpc.WithBlock())
	util.ErrorCheck(err)
	defer conn.Close()

	bytes, err := proto.Marshal(&headers)
	// 传送metadata
	md := metadata.Pairs("audio_meta", b64.StdEncoding.EncodeToString(bytes))

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
				log.Printf("asr result can get: %s", in.AudioFragment.Result)
			} else {
				log.Printf("asr result error")
			}
		}
	}()

	audioFileStream, err := os.Open(audioFile)
	util.ErrorCheck(err)
	defer audioFileStream.Close()
	bufferReader := bufio.NewReader(audioFileStream)

	sendPackageSize := int(headers.SendPerSeconds * float64(sampleRate) * 2)
	audioBytes := make([]byte, sendPackageSize)

	for {
		numBytesRead, err := bufferReader.Read(audioBytes)
		if err == io.EOF {
			break
		}
		log.Printf("%d bytes", numBytesRead)

		request := protogen.AudioFragmentRequest{
			AudioData: audioBytes,
		}

		if err := stream.Send(&request); err != nil {
			log.Fatalf("Failed to send a audio stream: %v", err)
		}
	}

	stream.CloseSend()
	<-waitc
}

func main() {
	flag.Parse()
}
