package main

import (
	pb "../protogen"
	"bufio"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"flag"
	"github.com/golang/protobuf/proto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"
	"io"
	"log"
	"os"
	"time"
	b64 "encoding/base64"
)

var (
	serverAddr         = flag.String("server_addr", "127.0.0.1:8051", "The server address in the format of host:port")
)

type Product struct {
	name, productId	string
	sampleRate	int
}

var	CUSTOMER_SERVICE = Product{name: "客服模型", productId: "1903", sampleRate: 8000}
var	CUSTOMER_SERVICE_TOUR = Product{name: "客服模型：旅游领域", productId: "1904", sampleRate: 8000}
var	CUSTOMER_SERVICE_STOCK = Product{name: "客服模型：股票领域", productId: "1905", sampleRate: 8000}
var	CUSTOMER_SERVICE_FINANCE = Product{name: "客服模型：金融领域", productId: "1906", sampleRate: 8000}
var	CUSTOMER_SERVICE_ENERGY = Product{name: "客服模型：能源领域", productId: "1907", sampleRate: 8000}
var	INPUT_METHOD = Product{name: "输入法模型", productId: "888", sampleRate: 16000}
var	FAR_FIELD = Product{name: "远场模型", productId: "1888", sampleRate: 16000}
var	FAR_FIELD_ROBOT = Product{name: "远场模型：机器人领域", productId: "1889", sampleRate: 16000}
var	SPEECH_SERVICE = Product{name: "演讲模型：听清", productId: "1912", sampleRate: 16000}

var product = CUSTOMER_SERVICE_FINANCE

func check(e error) {
	if e != nil {
		log.Fatalf("encount an error: %v", e)
	}
}

func hashToken(username string, password string, time string) string {
	hash := sha256.New()
	hash.Write([]byte(username+password+time))
	bs := hash.Sum(nil)
	hexBs := hex.EncodeToString(bs)
	return hexBs
}

func generateInitRequest() pb.InitRequest {

	content := pb.InitRequest{
		EnableLongSpeech: true,
		EnableChunk:      true,
		EnableFlushData:  true,
		ProductId:        product.productId,
		SamplePointBytes: 1,
		SendPerSeconds:   0.02,
		SleepRatio:       1,
		AppName:          "go",
		LogLevel:         0,
		UserName:         "123",
	}
	password := "123"
	nowTime := time.Now().Format("2006-01-02T15:04:05Z")
	content.ExpireTime = nowTime
	content.Token = hashToken(content.UserName, password, nowTime)
	return content
}

func main() {
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

	f, err := os.Open("testaudio/bj8k.wav")
	bufferReader := bufio.NewReader(f)

	sendPackageSize := int(headers.SendPerSeconds * float64(product.sampleRate) * 2)
	b1 := make([]byte, sendPackageSize)

	for {
		numBytesRead, err := bufferReader.Read(b1)
		if err == io.EOF {
			break
		}
		log.Printf("%d bytes", numBytesRead)

		request := pb.AudioFragmentRequest{
			AudioData:     b1,
		}

		if err := stream.Send(&request); err != nil {
			log.Fatalf("Failed to send a audio stream: %v", err)
		}
	}

	stream.CloseSend()
	<-waitc
}
