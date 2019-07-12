# -*-coding:utf-8-*-

from baidu_acu_asr.asr_client import AsrClient
from baidu_acu_asr.asr_product import AsrProduct
import time
import logging

import baidu_acu_asr.audio_streaming_pb2


def run():
    """
    添加失败重传
    :return:
    """
    for i in range(1):
        client = AsrClient(url, port, product_id, enable_flush_data,
                           log_level=log_level,
                           send_per_seconds=0.01,
                           user_name=user_name,
                           password=password)

        responses = client.get_result("/Users/xiashuai01/Downloads/10s.wav")
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


if __name__ == '__main__':
    logging.basicConfig(filename="asr_result.log")
    log_level = 0

    url = "10.136.172.23"
    port = "8052"
    product_id = AsrProduct.CUSTOMER_SERVICE_TOUR
    enable_flush_data = True
    user_name = "admin"
    password = "1234567809"

    run()