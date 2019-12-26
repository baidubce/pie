## 支持的语音格式
原始 PCM 的录音参数必须符合 8k/16k 采样率、16bit 位深、单声道，支持的格式有：pcm（不压缩）、wav（不压缩，pcm编码）、amr（压缩格式）。
## 快速入门
### SDK安装
#### online
执行`pip install baidu-acu-asr`安装sdk，创建新的python文件，示例代码如下(也可见client_demo.py)
#### offline
```
## 如果本地机器不存在pip
tar zxvf pip-19.0.3.tar.gz
cd pip-19.0.3
python setup.py install

## 安装，根据机器类型进相应的目录，目前支持mac和centos
cd [os]-install
for file in `ls`;do pip install --no-deps $file;done
```

### 文件说明
- client-demo-simple.py

仅用于测试离线音频识别

- client_demo_directory_multi_process.py

多进程识别文件夹中的音频文件，识别结果保存在每个音频文件名加.txt的文件中

- client_demo_directory_multi_thread.py

多线程识别文件夹中的音频文件，识别结果保存在每个音频文件名加.txt的文件中

- client_demo_multi_thread.py

多线程识别同一个音频，用于测试用

- client-demo.py

包含了mac麦克风音频流识别、url流识别、fifo流识别、离线音频识别多个识别方法

### 参数说明

`client = AsrClient("127.0.0.1", "8051", enable_flush_data=False)`初始化时可选传入的参数列表如下：

|参数| 类型 | 默认值 | 描述 |
|---|---|---|---|
| enable_chunk | bool | True | 是否允许chunk | 
| enable_long_speech | bool | True | 是否允许长语音 | 
| enable_flush_data | bool | True | 是否连续输出，False表示一次只输出一段识别的结果 | 
| product_id | string | 1903 | 对应解码器的模型 |
| sample_point_bytes | int | 2 | 采样点字节数，可根据音频的具体参数调整 |
| send_per_seconds | double | 0.16 | 设置发包间隔时间，发包大小会根据此值计算 |
| sleep_ratio | double  | 1 | ASR识别时长间隔 |
| app_name | String  | python | 可自己设定，log查看用 |
| log_level | int  | 4 | log级别，0：Trace，1：Debug， 2：Info，3：Warning，4：Error，5：Fatal，6：关闭log |
| user_name | String  | python | 用户名，服务端预分配 |
| expire_time | String  | python | 超时时间，表示该次流式识别有效时间，为UTC时间，例:2019-04-25T12:41:16Z |
| token | String  | python | user_name+password+expire_time 通过 sha256 生成的token |

返回数据参数详情：

|参数| 类型  | 描述 |
|---|---|---|
| error_code | int | 错误码，0表示请求成功，否则请求失败，具体错误码详情见下 | 
| error_message | string | 错误详情，请求成功该值为空 |
| start_time | string | 识别出来的文字对应音频开始时间 |
| end_time | string | 识别出来的文字对应音频结束时间 |
| result | string | 识别出来的文字 |
| completed | bool | 是否一段话结束 |
| serial_num | string| 请求唯一标识 |