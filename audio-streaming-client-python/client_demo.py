# -*-coding:utf-8-*-
import threading
from baidu_acu_asr.asr_client import AsrClient
import os
import time
import logging
from pyaudio import PyAudio, paInt16


# 产生流（mac上麦克风读取音频流，需要先brew install portaudio）
def record_micro():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
    NUM_SAMPLES = 2560  # pyaudio内置缓冲大小
    SAMPLING_RATE = 8000  # 取样频率
    pa = PyAudio()
    stream = pa.open(format=paInt16, channels=1, rate=SAMPLING_RATE, input=True, frames_per_buffer=NUM_SAMPLES)
    # yield stream
    while True:
        yield client.generate_stream_request(stream.read(NUM_SAMPLES))


# 产生流（本地音频流）
def generate_file_stream():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
    file_path = "/Users/xiashuai01/Downloads/10s.wav"
    if not os.path.exists(file_path):
        logging.info("%s file is not exist, please check it!", file_path)
        os._exit(-1)
    file = open(file_path, "r")
    content = file.read(2560)
    while len(content) > 0:
        yield client.generate_stream_request(content)
        content = file.read(2560)


def run():
    while True:
        client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
        responses = client.get_result("/Users/xiashuai01/Documents/null/20180612-144050_N00000013052_20318805_918186533085_cc-ali-0-1528785650.1122161.wav")

        try:
            for response in responses:
                logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)
            break
        except:
            # 如果出现异常，此处需要重试当前音频
            logging.error("connect to server error, will create a new channel and retry audio!")
            time.sleep(0.5)


def run_stream():
    client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
    responses = client.get_result_by_stream(record_micro())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)


if __name__ == '__main__':
    logging.basicConfig(filename="asr_result.log")
    url = "172.18.53.16"
    port = "30050"
    log_level = 0
    product_id = "1903"
    enable_flush_data = True

    # 传送文件
    run()
    # 传送流
    # run_stream()
    # 多线程运行
    # for i in range(100):
    #     print(i)
    #     t = threading.Thread(target=run, args=[])
    #     t.start()




