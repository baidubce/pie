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

### 读取本地文件
```python
# -*-coding:utf-8-*-
def run():
    """
    添加失败重传
    :return:
    """
    for i in range(30):
        client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level, user_name=user_name, password=password)
        responses = client.get_result("/Users/xiashuai01/Downloads/300s.wav")

        try:
            for response in responses:
                logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)
            break
        except:
            # 如果出现异常，此处需要重试当前音频
            logging.error("connect to server error, will create a new channel and retry audio! times : %d", i + 1)
            time.sleep(0.5)
```

### 读取流文件
```python
def generate_file_stream():
    """
    产生流（本地音频流）
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level, send_per_seconds=0.16, user_name=user_name, password=password)
    file_path = "/Users/xiashuai01/Downloads/300s.wav"
    if not os.path.exists(file_path):
        logging.info("%s file is not exist, please check it!", file_path)
        os._exit(-1)
    file = open(file_path, "r")
    content = file.read(320)
    while len(content) > 0:
        yield client.generate_stream_request(content)
        content = file.read(320)
        

def run_stream():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level, user_name=user_name, password=password)
    responses = client.get_result_by_stream(generate_file_stream())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)
``` 
读取mac上麦克风的音频流数据
```python
def record_micro():
    """
    产生流（mac上麦克风读取音频流，需要先brew install portaudio）
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
    NUM_SAMPLES = 2560  # pyaudio内置缓冲大小
    SAMPLING_RATE = 8000  # 取样频率
    pa = PyAudio()
    stream = pa.open(format=paInt16, channels=1, rate=SAMPLING_RATE, input=True, frames_per_buffer=NUM_SAMPLES)
    # yield stream
    while True:
        yield client.generate_stream_request(stream.read(NUM_SAMPLES))


def run_stream():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
    responses = client.get_result_by_stream(record_micro())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)
```

### 读取URL上的流
```python
def read_streaming_from_url():
    print("streaming reading")
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level, user_name=user_name, password=password)
    data = urllib2.urlopen(audio_url)
    while True:
        yield client.generate_stream_request(data.read(size=2560))


def run_url_streaming():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level, user_name=user_name, password=password)
    responses = client.get_result_by_stream(read_streaming_from_url())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)
```

### 参数说明

`client = AsrClient("172.18.53.17", "31051", enable_flush_data=False)`初始化时可选传入的参数列表如下：

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