# 依赖安装

```shell
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
```

# proto代码生成方式

## 新版本
```shell script
protoc --go_out=. --go_opt=paths=source_relative --go-grpc_out=. --go-grpc_opt=paths=source_relative audio_streaming.proto
```

## 老版本
```shell script
protoc -I ./ audio_streaming.proto --go_out=plugins=grpc:.
```

