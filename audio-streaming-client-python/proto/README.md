# 依赖安装
```shell
pip install protobuf
pip install grpcio-tools
```

# 生成代码
```shell
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. audio_streaming.proto
```
生成的`audio_streaming_pb2_grpc.py` 和 `audio_streaming_pb2.py` 文件拷贝到 baidu_acu_asr 文件夹中

# 优化
```python
# audio_streaming_pb2_grpc.py 文件

import audio_streaming_pb2 as audio__streaming__pb2
# 修改为
from . import audio_streaming_pb2 as audio__streaming__pb2
```