# -*-coding:utf-8-*-
import threading
from baidu_acu_asr.asr_client import AsrClient
from baidu_acu_asr.asr_product import AsrProduct
import os
import time
import logging
from pyaudio import PyAudio, paInt16
import urllib
import baidu_acu_asr.audio_streaming_pb2


def record_micro():
    """
    产生流（mac上麦克风读取音频流，需要先brew install portaudio）
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       user_name=user_name,
                       password=password)
    NUM_SAMPLES = 2560  # pyaudio内置缓冲大小
    SAMPLING_RATE = 8000  # 取样频率
    pa = PyAudio()
    stream = pa.open(format=paInt16, channels=1, rate=SAMPLING_RATE, input=True, frames_per_buffer=NUM_SAMPLES)
    # yield stream
    while True:
        yield client.generate_stream_request(stream.read(NUM_SAMPLES))


def generate_file_stream():
    """
    产生流（本地音频流）
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       send_per_seconds=0.16,
                       user_name=user_name,
                       password=password)
    file_path = "/Users/xiashuai01/TranFile/tem/3.wav"
    if not os.path.exists(file_path):
        logging.info("%s file is not exist, please check it!", file_path)
        os._exit(-1)
    file = open(file_path, "r")
    content = file.read(320)
    while True:
        yield client.generate_stream_request(content)
        content = file.read(320)


def run_file_stream():
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       user_name=user_name,
                       password=password)
    responses = client.get_result_by_stream(generate_file_stream())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)


def general_fifo_stream():
    """
    读取管道数据
    1.新建管道：mkfifo pipe.wav
    2.获取流存入管道：ffmpeg -i "http://path/of/video/stream" -vn -acodec pcm_s16le -ac 1 -ar 8000 -f wav pipe:1 > pipe.wav
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       send_per_seconds=0.16,
                       user_name=user_name,
                       password=password)
    rf = os.open("/Users/xiashuai01/TranFile/tem/pipe.wav", os.O_RDONLY)
    while True:
        stream = os.read(rf, 320)
        yield client.generate_stream_request(stream)


def run_fifo_stream():
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       user_name=user_name,
                       password=password)
    responses = client.get_result_by_stream(general_fifo_stream())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)


def run():
    """
    添加失败重传
    :return:
    """
    for i in range(5):
        client = AsrClient(url, port, product_id, enable_flush_data,
                           log_level=log_level,
                           send_per_seconds=0.01,
                           user_name=user_name,
                           password=password)
        responses = client.get_result("/path/of/audio.wav")

        try:
            for response in responses:
                if response.type == baidu_acu_asr.audio_streaming_pb2.FRAGMENT_DATA:
                    logging.info("%s\t%s\t%s\t%s",
                                 response.audio_fragment.start_time,
                                 response.audio_fragment.end_time,
                                 response.audio_fragment.result,
                                 response.audio_fragment.serial_num)
                else:
                    logging.warning("type is: %d", response.type)

            break
        except Exception as ex:
            # 如果出现异常，此处需要重试当前音频
            logging.error("encounter an error: %s, will create a new channel and retry audio! times : %d",
                          ex.message, i + 1)
            time.sleep(0.5)


def run_stream():
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       send_per_seconds=0.01,
                       user_name=user_name,
                       password=password)
    responses = client.get_result_by_stream(record_micro())
    for response in responses:
        if response.type == baidu_acu_asr.audio_streaming_pb2.FRAGMENT_DATA:
            logging.info("%s\t%s\t%s\t%s",
                         response.audio_fragment.start_time,
                         response.audio_fragment.end_time,
                         response.audio_fragment.result,
                         response.audio_fragment.serial_num)
        else:
            logging.warning("type is: %d", response.type)


def read_streaming_from_url():
    """
    读取url上的流
    :return:
    """
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       user_name=user_name,
                       password=password)
    data = urllib.request.urlopen(audio_url)
    while True:
        yield client.generate_stream_request(data.read(size=2560))


def run_url_streaming():
    client = AsrClient(url, port, product_id, enable_flush_data,
                       log_level=log_level,
                       user_name=user_name,
                       password=password)
    responses = client.get_result_by_stream(read_streaming_from_url())
    for response in responses:
        # for res in responses:
        logging.info("%s\t%s\t%s\t%s", response.start_time, response.end_time, response.result, response.serial_num)


if __name__ == '__main__':
    logging.basicConfig(filename="asr_result.log")
    log_level = 0

    url = "127.0.0.1"
    port = "8050"
    product_id = AsrProduct.CUSTOMER_SERVICE_FINANCE
    enable_flush_data = True
    user_name = "abc"
    password = "123"

    # audio_url = "http://onlinebjplay.baidudomainbcd.com/aitest/ai_stream.flv"
    # run_url_streaming()

    # 读取管道数据
    # run_fifo_stream()
    # 传送文件/Users/xiashuai01/MyLibrary/anaconda2/bin:/usr/local/opt/sqlite/bin:/Users/xiashuai01/bin:/bin:/usr/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin
    run()
    # 传送流
    # run_stream()
    # 多线程运行
    # for i in range(100):
    #     print(i)
    #     t = threading.Thread(target=run, args=[])
    #     t.start()




