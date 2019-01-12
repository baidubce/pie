from __future__ import print_function

import grpc

import audio_streaming_pb2
import audio_streaming_pb2_grpc
import header_manipulator_client_interceptor
import base64
import os
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')


class AsrClient(object):
    """ version modify log,
    from version 1.0.7: change init method, move product_id and enable_flush_data to be must params;
    """

    request = audio_streaming_pb2.InitRequest()

    def __init__(self, server_ip, port, product_id, enable_flush_data,
                 enable_chunk=True,
                 enable_long_speech=True,
                 sample_point_bytes=2,
                 send_per_seconds=0.16,
                 sleep_ratio=1,
                 app_name='python',
                 log_level=4,
                 send_package_size=320):
        self.server_ip = server_ip
        self.port = port
        self.host = server_ip + ":" + port
        self.request.enable_chunk = enable_chunk
        self.request.enable_long_speech = enable_long_speech
        self.request.enable_flush_data = enable_flush_data
        self.request.product_id = product_id
        self.request.sample_point_bytes = sample_point_bytes
        self.request.send_per_seconds = send_per_seconds
        self.request.sleep_ratio = sleep_ratio
        self.request.app_name = app_name
        self.request.log_level = log_level
        self.send_package_size = send_package_size

    def generate_file_stream(self, file_path):
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
        return audio_streaming_pb2.AudioFragmentRequest(audio_data=file_stream)

    def get_result(self, file_path):
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
        header_adder_interceptor = header_manipulator_client_interceptor.header_adder_interceptor(
            'audio_meta', base64.b64encode(self.request.SerializeToString()))
        with grpc.insecure_channel(self.host) as channel:
            intercept_channel = grpc.intercept_channel(channel,
                                                       header_adder_interceptor)
            stub = audio_streaming_pb2_grpc.AsrServiceStub(intercept_channel)
            responses = stub.send(file_steam)
            for response in responses:
                yield response
