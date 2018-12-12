### 编译proto文件方法
#### Mac
```shell
python -m grpc_tools.protoc -I./ --python_out=. --grpc_python_out=. ./audio_streaming.proto
```

### 运行
> 运行之前需要在conf.py中修改grpc-server地址、音频文件路径以及相关初始化的参数信息
