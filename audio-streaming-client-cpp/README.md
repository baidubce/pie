## 使用要求
gcc4.8.2以上
## 使用方法
1. audio-streaming-client-cpp目录下执行make，生成./lib/libaudio_streaming_client.so
2. 进入到demo目录，执行make命令，生成
3. 在audio-streaming-client-cpp中执行命令：./demo/client_demo 1903 baiduai.cloud:8443 user pwd 2019-06-25T12:41:16Z

## 重要参数说明
product_id : 对应解码器的模型
user_name : 用户名，服务端预分配
expire_time : 超时时间，表示该次流式识别有效时间，UTC时间，例:2019-04-25T12:41:16Z
app_name : 可自己设定，log查看用，包含字母数字下划线，以字线或下划线开头