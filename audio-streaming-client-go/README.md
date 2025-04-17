# 运行
```shell script
# 可通过 go run main.go -h 查看可输入的命令
# mac 文件识别
go run main_darwin.go -server_addr=127.0.0.1:8051 -username 123 -password 123 

# mac 麦克风识别
go run main_microphone_darwin.go -server_addr=127.0.0.1:8051 -username 123 -password 123 -run_type microphone

# linux 文件识别
go run main_linux.go -server_addr=127.0.0.1:8051 -username 123 -password 123 

# 编译二进制
GOOS=darwin GOARCH=arm64 go build
```

# 注
mac上运行之前，需要执行
```shell
brew install pkg-config
brew install portaudio
```
