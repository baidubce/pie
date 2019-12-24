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
        client = AsrClient(url, port, None, enable_flush_data,
                           product_id=product_id,
                           sample_rate=sample_rate,
                           log_level=log_level,
                           send_per_seconds=0.01,
                           user_name=user_name,
                           password=password)

        responses = client.get_result("testaudio/xeq16k.wav")
        try:
            for response in responses:
                if response.type == baidu_acu_asr.audio_streaming_pb2.FRAGMENT_DATA:
                    logging.info("%s\t%s\t%s\t%s",
                                 response.audio_fragment.start_time,
                                 response.audio_fragment.end_time,
                                 response.audio_fragment.result,
                                 response.audio_fragment.serial_num)
                else:
                    logging.warning("type is: %d, error code: %d, error message: %d",
                                    response.type, response.error_code, response.error_message)

            break
        except Exception as ex:
            # 如果出现异常，此处需要重试当前音频
            logging.error("encounter an error: %s, will create a new channel and retry audio! times : %d",
                          ex.message, i + 1)
            time.sleep(0.5)


if __name__ == '__main__':
    logging.basicConfig(filename="asr_result.log")
    log_level = 0

    url = "127.0.0.1"
    port = "8050"
    # product_id = AsrProduct.INPUT_METHOD
    product_id = "888"
    sample_rate = 16000
    enable_flush_data = True
    user_name = "abc"
    password = "123"

    run()