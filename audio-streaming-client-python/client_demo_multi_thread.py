# -*-coding:utf-8-*-
from baidu_acu_asr.asr_client import AsrClient
import threading
import sys
import logging
import time
from baidu_acu_asr.asr_product import AsrProduct
import baidu_acu_asr.audio_streaming_pb2
class demo:

    def run(self, repeat_num=1):
        i = 0
        # ip和端口可根据需要修改
        url = "180.76.107.131"
        port = "8050"
        product_id = AsrProduct.CUSTOMER_SERVICE_FINANCE
        enable_flush_data = True
        log_level = 0
        user_name = "abc"
        password = "123"
        client = AsrClient(url, port, product_id, enable_flush_data, user_name, password, log_level=log_level)
        name = threading.currentThread().getName()
        audio_path = "/Users/lijialong02/code/client/data/10s.wav"
        while i < repeat_num:
            try:
                response = client.get_result(audio_path)
                for res in response:
                    if res.type == baidu_acu_asr.audio_streaming_pb2.FRAGMENT_DATA:
                        logging.info(name + "\t" + str(res.error_code) + "\t" + res.error_message + "\t" + res.audio_fragment.start_time + "\t" +
                            res.audio_fragment.end_time + "\t" + res.audio_fragment.result + "\t" + res.audio_fragment.serial_num + "\t" +
                            str(res.audio_fragment.completed))
                    else:
                        logging.info("type is: %d", res.type)
                i += 1
            except:
                # 如果出现异常，此处需要重试当前音频
                logging.error("connect to server error, will create a new channel and retry audio!")
                time.sleep(0.5)
                client = AsrClient(url, port, product_id, enable_flush_data, user_name, password, log_level=log_level)

if __name__ == '__main__':
    logging.basicConfig(filename="asr_result.log")
    demo1 = demo()
    thread_num = sys.argv[1]
    repeat_num = sys.argv[2]
    threads = []
    start_time = time.time()
    for i in range(int(thread_num)):
        t = threading.Thread(target=demo1.run, args=[int(repeat_num)])
        time.sleep(0.5)
        t.start()
        threads.append(t)

    for thread in threads:
        thread.join()
    logging.info("complete!")
    logging.info("use time: %s", str(time.time() - start_time))
