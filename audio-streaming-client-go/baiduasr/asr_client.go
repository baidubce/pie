package main

import (
	pb "../protogen"
	"bufio"
	"context"
	"crypto/sha256"
	b64 "encoding/base64"
	"encoding/hex"
	"flag"
	"github.com/golang/protobuf/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"io"
	"log"
	"os"
	"time"
)

var (
	serverAddr = flag.String("server_addr", "127.0.0.1:8051", "The server address in the format of host:port")
)

var username, password, productId, audioFile string
var sampleRate int
var enableFlushData bool

func init() {
	flag.StringVar(&username, "username", "123", "The username to login streaming server")
	flag.StringVar(&password, "password", "123", "The password to login streaming server")
	flag.StringVar(&productId, "product_id", "1903", "The pid for ASR engine'")
	flag.IntVar(&sampleRate, "sample_rate", 8000, "The sample rate for ASR engine")
	flag.StringVar(&audioFile, "audio_file", "testaudio/bj8k.wav", "The audio file path")
	flag.BoolVar(&enableFlushData, "enable_flush_data", true, "enable flush data")
}

type Product struct {
	name, productId string
	sampleRate      int
}

func check(e error) {
	if e != nil {
		log.Fatalf("encount an error: %v", e)
	}
}

func hashToken(time string) string {
	hash := sha256.New()
	hash.Write([]byte(username + password + time))
	bs := hash.Sum(nil)
	hexBs := hex.EncodeToString(bs)
	return hexBs
}

func generateInitRequest() pb.InitRequest {

	content := pb.InitRequest{
		EnableLongSpeech: true,
		EnableChunk:      true,
		EnableFlushData:  enableFlushData,
		ProductId:        productId,
		SamplePointBytes: 1,
		SendPerSeconds:   0.02,
		SleepRatio:       1,
		AppName:          "go",
		LogLevel:         0,
		UserName:         username,
	}
	nowTime := time.Now().Format("2006-01-02T15:04:05Z")
	content.ExpireTime = nowTime
	content.Token = hashToken(nowTime)
	return content
}

func main() {
	flag.Parse()

	conn, err := grpc.Dial(*serverAddr, grpc.WithInsecure(), grpc.WithBlock())
	check(err)
	defer conn.Close()
	headers := generateInitRequest()

	bytes, err := proto.Marshal(&headers)
	// 传送metadata
	md := metadata.Pairs("audio_meta", b64.StdEncoding.EncodeToString(bytes))

	client := pb.NewAsrServiceClient(conn)

	ctx := metadata.NewOutgoingContext(context.Background(), md)
	stream, err := client.Send(ctx)
	check(err)
	waitc := make(chan struct{})

	go func() {
		for {
			in, err := stream.Recv()
			if err == io.EOF {
				// read done.
				close(waitc)
				return
			}
			check(err)
			if in != nil {
				log.Printf("asr result can get: %s", in.AudioFragment.Result)
			} else {
				log.Printf("asr result error")
			}
		}
	}()

	audioFile, err := os.Open(audioFile)
	bufferReader := bufio.NewReader(audioFile)

	sendPackageSize := int(headers.SendPerSeconds * float64(sampleRate) * 2)
	audioBytes := make([]byte, sendPackageSize)

	for {
		numBytesRead, err := bufferReader.Read(audioBytes)
		if err == io.EOF {
			break
		}
		log.Printf("%d bytes", numBytesRead)

		request := pb.AudioFragmentRequest{
			AudioData: audioBytes,
		}

		if err := stream.Send(&request); err != nil {
			log.Fatalf("Failed to send a audio stream: %v", err)
		}
	}

	stream.CloseSend()
	<-waitc
}
