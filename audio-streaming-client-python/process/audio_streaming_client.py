# -*-coding:utf-8-*-
#################################
#
# Copyright (c) 2018
#
#################################

from __future__ import print_function

import grpc

import audio_streaming_pb2
import audio_streaming_pb2_grpc
import header_manipulator_client_interceptor
import base64
from conf import conf


def generate_file_stream():
    file = open(
        basic_conf[
            'audio_file_path'],
        "r")
    content = file.read(1024)
    while len(content) > 0:
        yield audio_streaming_pb2.AudioFragmentRequest(audio_data=content)
        content = file.read(1024)
    file.close()


def get_init_request():
    request = audio_streaming_pb2.InitRequest()
    request.enable_chunk = init_request_conf['enable_chunk']
    request.enable_long_speech = init_request_conf['enable_long_speech']
    request.enable_flush_data = init_request_conf['enable_flush_data']
    request.product_id = init_request_conf['product_id']
    request.sample_point_bytes = init_request_conf['sample_point_bytes']
    request.send_per_seconds = init_request_conf['send_per_seconds']
    request.sleep_ratio = init_request_conf['sleep_ratio']
    request.app_name = init_request_conf['app_name']
    request.log_level = init_request_conf['log_level']
    return request


def run():
    init_request = get_init_request()
    header_adder_interceptor = header_manipulator_client_interceptor.header_adder_interceptor(
        'audio_meta', base64.b64encode(init_request.SerializeToString()))

    with grpc.insecure_channel(basic_conf['server']) as channel:
        intercept_channel = grpc.intercept_channel(channel,
                                                   header_adder_interceptor)
        stub = audio_streaming_pb2_grpc.AsrServiceStub(intercept_channel)
        responses = stub.send(generate_file_stream())
        for response in responses:
            print("start_time\tend_time\tresult")
            print(response.start_time + "\t" + response.end_time + "\t" + response.result)
        print(response.start_time + "\t" + response.end_time + "\t" + response.result + "response end!")


if __name__ == '__main__':
    basic_conf = conf['basic_conf']
    init_request_conf = conf['init_request_conf']
    run()