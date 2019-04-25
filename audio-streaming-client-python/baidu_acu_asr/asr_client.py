# -*- coding: utf-8 -*-

from __future__ import print_function
from asr_product import ProductMap

import grpc
import audio_streaming_pb2
import audio_streaming_pb2_grpc
import header_manipulator_client_interceptor
import base64
import os
import logging
import hashlib
import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')


class AsrClient(object):
    """ version modify log,
    from version 1.0.7: change init method, move product_id and enable_flush_data to be must params;
    """

    request = audio_streaming_pb2.InitRequest()

    def __init__(self, server_ip, port, product, enable_flush_data,
                 enable_chunk=True,
                 enable_long_speech=True,
                 sample_point_bytes=2,
                 send_per_seconds=0.02,
                 sleep_ratio=1,
                 app_name='python',
                 log_level=4,
                 user_name=None,
                 password=None):
        # asr流式服务器的地址，私有化版本请咨询供应商
        self.server_ip = server_ip
        # asr流式服务的端口，私有化版本请咨询供应商
        self.port = port
        self.host = server_ip + ":" + port
        self.request.enable_chunk = enable_chunk
        # 是否允许长音频
        self.request.enable_long_speech = enable_long_speech
        # 是否返回中间翻译结果
        self.request.enable_flush_data = enable_flush_data
        self.request.product_id = product.value[1]
        self.request.sample_point_bytes = sample_point_bytes
        # 指定每次发送的音频数据包大小，通常不需要修改
        self.request.send_per_seconds = send_per_seconds
        self.request.sleep_ratio = sleep_ratio
        # asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
        self.request.app_name = app_name
        # 服务端的日志输出级别
        self.request.log_level = log_level
        if user_name and password is not None:
            # 用户名
            self.request.user_name = user_name
            # 超时时间 UTC 格式
            expire_time = (datetime.datetime.utcnow()+datetime.timedelta(hours=1)).strftime('%Y-%m-%dT%H:%M:%SZ')
            self.request.expire_time = expire_time
            # user_name password expire_time 生成的token
            self.request.token = hashlib.sha256(user_name + password + expire_time).hexdigest()
        # 每次发送的音频字节数
        self.send_package_size = int(send_per_seconds * product.value[2] * sample_point_bytes)

    def generate_file_stream(self, file_path):
        """
        从音频文件中读取流
        :param file_path: 音频文件路径
        :return: 文件流的迭代
        """
        if not os.path.exists(file_path):
            logging.info("%s file is not exist, please check it!", file_path)
            os._exit(-1)
        file = open(file_path, "r")
        content = file.read(self.send_package_size)
        while len(content) > 0:
            yield audio_streaming_pb2.AudioFragmentRequest(audio_data=content)
            content = file.read(self.send_package_size)
        file.close()

    def generate_stream_request(self, file_stream):
        """
        通过音频流产生request对象
        :param file_stream: 字节流
        :return: request对象
        """
        return audio_streaming_pb2.AudioFragmentRequest(audio_data=file_stream)

    def get_result(self, file_path):
        """
        通过文件路径获取最终解码结果的迭代器
        :param file_path:
        :return: response的迭代
        """
        header_adder_interceptor = header_manipulator_client_interceptor.header_adder_interceptor(
            'audio_meta', base64.b64encode(self.request.SerializeToString()))
        with grpc.insecure_channel(target=self.host, options=[('grpc.keepalive_timeout_ms', 1000000), ]) as channel:
            intercept_channel = grpc.intercept_channel(channel,
                                                       header_adder_interceptor)
            stub = audio_streaming_pb2_grpc.AsrServiceStub(intercept_channel)
            responses = stub.send(self.generate_file_stream(file_path), timeout=100000)
            for response in responses:
                yield response

    def get_result_by_stream(self, file_steam):
        """
        通过音频流获取解码结果的迭代器
        :param file_steam: 字节流
        :return: response结果的迭代
        """
        header_adder_interceptor = header_manipulator_client_interceptor.header_adder_interceptor(
            'audio_meta', base64.b64encode(self.request.SerializeToString()))
        with grpc.insecure_channel(self.host) as channel:
            intercept_channel = grpc.intercept_channel(channel,
                                                       header_adder_interceptor)
            stub = audio_streaming_pb2_grpc.AsrServiceStub(intercept_channel)
            responses = stub.send(file_steam)
            for response in responses:
                yield response
