## 使用要求
gcc4.8.2以上
## 使用方法
1. audio-streaming-client-cpp目录下执行make，生成./lib/libaudio_streaming_client.so
2. 进入到demo目录，执行make命令，生成
3. 在audio-streaming-client-cpp中执行命令：./demo/client_demo 1903 baiduai.cloud:8443 user pwd 2019-06-25T12:41:16Z 0

## 重要参数说明
1. product_id : 对应解码器的模型
2. user_name : 用户名，服务端预分配
3. expire_time : 超时时间，表示该次流式识别有效时间，UTC时间，例:2019-04-25T12:41:16Z
4. app_name : 可自己设定，log查看用，包含字母数字下划线，以字线或下划线开头

## 已有依赖及编译结果下载
http://acg-voice.su.bcebos.com/ori/asr/audio-streaming-client-cpp_20220218.zip?authorization=bce-auth-v1%2Fe137fbe330324a5599fa40ba2fa7192a%2F2022-02-22T03%3A00%3A32Z%2F-1%2Fhost%2Fe23921c8139198b3df00bc1375419e98129ca9a7db3f002832c73699147e6337