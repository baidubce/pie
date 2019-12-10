# 运行
```shell script
# 可通过 go run baiduasr/asr_client.go -h 查看可输入的命令
go run baiduasr/asr_client.go -server_addr=127.0.0.1:8051 -username 123 -password 123 
```

# 依赖
```shell script
github.com/golang/protobuf/proto v1.3.2
google.golang.org/grpc 1.26.0-dev
```