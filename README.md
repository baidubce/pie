## 百度云流式语音识别客户端

### 项目结构
[audio-streaming-server-cpp](https://github.com/baidubce/pie/tree/master/audio-streaming-client-cpp)：c++版本client sdk

[audio-streaming-server-java](https://github.com/baidubce/pie/tree/master/audio-streaming-client-java)：java版本client sdk

[audio-streaming-server-python](https://github.com/baidubce/pie/tree/master/audio-streaming-client-python)：python版本client sdk

### Examples
在对应的目录下找到demo client运行：

[c++](https://github.com/baidubce/pie/blob/master/audio-streaming-client-cpp/samples)

[java](https://github.com/baidubce/pie/blob/master/audio-streaming-client-java/src/main/java/com/baidu/acu/pie/demo)

[python](https://github.com/baidubce/pie/blob/master/audio-streaming-client-python)

[android]()

### 参数说明
初始化参数

|参数| 类型 | 默认值 | 描述 |
|---|---|---|---|
| enable_chunk | bool | True | 是否允许chunk | 
| enable_long_speech | bool | True | 是否允许长语音 | 
| enable_flush_data | bool | True | 是否连续输出，False表示一次只输出一段识别的结果 | 
| product_id | string | 1903 | 对应解码器的模型 |
| sample_point_bytes | int | 2 | 采样点字节数，可根据音频的具体参数调整 |
| send_per_seconds | double | 0.16 | 设置发包间隔时间 |
| sleep_ratio | double  | 1 | 识别睡眠时长间隔，发包大小会根据此值计算 |
| app_name | String  | python | 可自己设定，log查看用 |
| log_level | int  | 4 | log级别，0：Trace，1：Debug， 2：Info，3：Warning，4：Error，5：Fatal，6：关闭log |

返回结果参数

|参数| 类型  | 描述 |
|---|---|---|
| error_code | int | 错误码，0表示请求成功，否则请求失败，具体错误码详情见下 | 
| error_message | string | 错误详情，请求成功该值为空 |
| start_time | string | 识别出来的文字对应音频开始时间 |
| end_time | string | 识别出来的文字对应音频结束时间 |
| result | string | 识别出来的文字 |
| completed | bool | 是否一段话结束 |
| serial_num | string| 请求唯一标识 |

### Issues
相关问题可以直接提交issue，也可以提交给百度同学

### Contact Us
使用之前请联系百度同学要到streaming server的ip和port、对应的product id，并且添加白名单信息（需要提供client出口ip，可以通过`curl cip.cc`获得）
