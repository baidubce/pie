## 百度云流式语音识别客户端

### 项目结构
- [audio-streaming-server-cpp](https://github.com/baidubce/pie/tree/master/audio-streaming-client-cpp)：c++/c版本client sdk

- [audio-streaming-server-java](https://github.com/baidubce/pie/tree/master/audio-streaming-client-java)：java版本client sdk

- [audio-streaming-server-python](https://github.com/baidubce/pie/tree/master/audio-streaming-client-python)：python版本client sdk

- [audio-streaming-server-c#](https://github.com/baidubce/pie/tree/master/audio-streaming-client-c#)：c#版本client sdk, beta version 

- [android-demo](https://github.com/baidubce/pie/tree/master/android-demo)：基于java sdk实现的android实时音频流识别的demo app

### 功能
本部分代码为asr streaming client端，支持的场景如下：

- 大音频文件的识别
- 音频流url的识别
- 管道音频流的识别
- 实时音频流的识别

### 常用参数
以下列举了常用的参数，具体参数可以参考对应目录的client

- url：asr streaming server端的ip（需要联系百度同学获取）
- port：asr streaming server端服务对应的端口号
- enable\_flush\_data：是否连续输出，False表示一次只输出每段话识别的结果
- product_id：每个product id对应一个后端解码器的模型
- send\_per\_seconds：设置server发包间隔时间，推荐值为0.02, 即20ms。发包大小会根据此值计算,计算方式为：发包大小 = send_per_seconds * 音频采样率 * 采样点字节数。对于8k音频，发包大小为320，16k音频，发包大小为640。
- sleep_ratio：默认为1，在send_per_seconds和发包大小都使用推荐值的情况下，代表了实时音频流的处理速率。如果要加速处理，可以适当减小sleep_ratio，比如sleep_ratio=0.5时，代表了以两倍速率进行处理。处理速率过块，可能会造成丟字。 正常情况下，推荐设置为1. 

### Examples
在对应的目录下找到demo client运行：

- [c++/c](https://github.com/baidubce/pie/blob/master/audio-streaming-client-cpp/samples)

- [java](https://github.com/baidubce/pie/blob/master/audio-streaming-client-java/src/main/java/com/baidu/acu/pie/demo)

- [python](https://github.com/baidubce/pie/blob/master/audio-streaming-client-python)

- [c#](https://github.com/baidubce/pie/blob/master/audio-streaming-client-c#)

- [android]()

### Issues
相关问题可以直接提交issue，也可以提交给百度同学

### Contact Us
使用之前请联系百度同学要到streaming server的ip和port、对应的product id，并且添加白名单信息（需要提供client出口ip，可以通过`curl cip.cc`获得）
