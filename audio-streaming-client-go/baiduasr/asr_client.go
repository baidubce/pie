package client

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	b64 "encoding/base64"
	"io"
	"io/ioutil"
	"log"
	"os"
	"strings"
	"time"

	flagUtil "github.com/baidubce/pie/audio-streaming-client-go/flag"
	"github.com/baidubce/pie/audio-streaming-client-go/protogen"
	"github.com/baidubce/pie/audio-streaming-client-go/util"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/proto"
)

// 处理音频文件音频流
func ReadFile(headers protogen.InitRequest) {
	opts := []grpc.DialOption{}
	// add https certificate
	if len(flagUtil.SSLPath) > 0 {
		certPool := x509.NewCertPool()
		caCert, err := ioutil.ReadFile(flagUtil.SSLPath)
		if err != nil {
			log.Fatalf("无法读取 CA 证书: %s", err)
		}

		if ok := certPool.AppendCertsFromPEM(caCert); !ok {
			log.Fatalf("添加 CA 证书到证书池失败")
		}

		// 创建 TLS 配置，使用 CA 证书，并指定期望的服务器名称（CN）
		tlsConfig := &tls.Config{
			RootCAs: certPool,
			// Replace with the server's CN
			ServerName: strings.Split(flagUtil.ServerAddr, ":")[0],
		}

		// 创建带有 TLS 配置的凭据
		creds := credentials.NewTLS(tlsConfig)

		opts = append(opts, grpc.WithTransportCredentials(creds))
	} else {
		opts = append(opts, grpc.WithInsecure())
	}
	opts = append(opts, grpc.WithBlock())

	conn, err := grpc.Dial(flagUtil.ServerAddr, opts...)
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
				log.Printf(" | start time: %s, end time: %s, result: %s, complete: %t, sn: %s, traceID: %s",
					in.AudioFragment.StartTime, in.AudioFragment.EndTime, in.AudioFragment.Result, in.AudioFragment.Completed, in.AudioFragment.SerialNum, in.TraceId)
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
			time.Sleep(time.Duration(20*flagUtil.SleepRatio) * time.Millisecond)
		}
	}

	stream.CloseSend()
	<-waitC
}
