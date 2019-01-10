## 使用方法
可以在本地执行`pip install baidu-acu-asr`安装sdk，创建新的python文件，示例代码如下(也可见client_demo.py)
### 本地文件
```python
# -*-coding:utf-8-*-
from baidu_acu_asr.AsrClient import AsrClient
import threading


def run():
    response = client.get_result("/Users/xiashuai01/Downloads/300s.wav")
    for res in response:
        print("start_time\tend_time\tresult")
        print(res.start_time + "\t" + res.end_time + "\t" + res.result)


if __name__ == '__main__':
    # ip和端口可根据需要修改
    client = AsrClient("172.18.53.17", "31051", enable_flush_data=False)
    run()
    # 多线程运行
    # for i in range(100):
    #     print(i)
    #     t = threading.Thread(target=run, args=[])
    #     t.start()
```

### 流文件
```python
from baidu_acu_asr.AsrClient import AsrClient
import os


def generate_file_stream():
    file_path = "/Users/xiashuai01/Downloads/10s.wav"
    if not os.path.exists(file_path):
        logging.info("%s file is not exist, please check it!", file_path)
        os._exit(-1)
    file = open(file_path, "r")
    content = file.read(2560)
    while len(content) > 0:
        yield client.generate_stream_request(content)
        content = file.read(2560)
        
        
def run_stream():
    responses = client.get_result_by_stream(generate_file_stream())
    for response in responses:
        # for res in responses:
        print("start_time\tend_time\tresult")
        print(response.start_time + "\t" + response.end_time + "\t" + response.result)

        
if __name__ == '__main__':
    product_id = "1903"
    enable_flush_data = False
    # ip和端口可根据需要修改
    client = AsrClient("180.76.107.131", "8050", product_id, enable_flush_data)
    run_stream()
``` 
读取mac上麦克风的音频流数据
```python
# -*-coding:utf-8-*-
import threading
from baidu_acu_asr.AsrClient import AsrClient
import os
from pyaudio import PyAudio, paInt16


# 产生流（mac上麦克风读取音频流，需要先brew install portaudio）
def record_micro():
    NUM_SAMPLES = 2560  # pyaudio内置缓冲大小
    SAMPLING_RATE = 8000  # 取样频率
    pa = PyAudio()
    stream = pa.open(format=paInt16, channels=1, rate=SAMPLING_RATE, input=True, frames_per_buffer=NUM_SAMPLES)
    # yield stream
    while True:
        yield client.generate_stream_request(stream.read(NUM_SAMPLES))


def run_stream():
    # responses = client.get_result_by_stream(generate_file_stream())
    responses = client.get_result_by_stream(record_micro())
    for response in responses:
        # for res in responses:
        print("start_time\tend_time\tresult")
        print(response.start_time + "\t" + response.end_time + "\t" + response.result)


if __name__ == '__main__':
    product_id = "1903"
    enable_flush_data = False
    # ip和端口可根据需要修改
    client = AsrClient("180.76.107.131", "8050", product_id, enable_flush_data)
    run_stream()
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
| send_per_seconds | double | 0.16 | 默认值即可 |
| sleep_ratio | double  | 1 | 识别睡眠时长间隔 |
| app_name | String  | python | 可自己设定，log查看用 |
| log_level | int  | 4 | log级别，0：Trace，1：Debug， 2：Info，3：Warning，4：Error，5：Fatal，6：关闭log |


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