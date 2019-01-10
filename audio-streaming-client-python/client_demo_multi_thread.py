# -*-coding:utf-8-*-
from baidu_acu_asr.AsrClient import AsrClient
import threading
import sys
import logging
import time


class demo:

    def run(self, repeat_num=1):
        i = 0
        # ip和端口可根据需要修改
        # client = AsrClient("10.136.172.23", "8051", enable_flush_data=False, log_level=6)
        url = "180.76.107.131"
        # port = "8300"
        # product_id = "-103"
        port = "8200"
        product_id = "1906"
        enable_flush_data = True
        log_level = 0
        client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)
        # client = AsrClient("172.18.53.16", "8050", product_id, enable_flush_data, log_level=3)
        name = threading.currentThread().getName()
        # audio_path = "/Users/xiashuai01/Downloads/1035_voice001_L8VV.wav"
        audio_path = "/Users/xiashuai01/Downloads/10s.wav"
        while i < repeat_num:
            try:
                response = client.get_result(audio_path)
                for res in response:
                    logging.info(name + "\t" + str(res.error_code) + "\t" + res.error_message + "\t" + res.start_time + "\t" +
                              res.end_time + "\t" + res.result + "\t" + res.serial_num + "\t" + str(res.completed))
                i += 1
            except:
                # 如果出现异常，此处需要重试当前音频
                logging.error("connect to server error, will create a new channel and retry audio!")
                time.sleep(0.5)
                client = AsrClient(url, port, product_id, enable_flush_data, log_level=log_level)


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
