# -*-coding:utf-8-*-
from baidu_acu_asr.AsrClient import AsrClient
import threading
import sys
import logging
import time


class demo:

    def run(self, repeat_num=1):
        i = 0
        # ip和端口可根据需要修改，sdk接口文档见http://agroup.baidu.com/abc_voice/md/article/1425870
        # client = AsrClient("10.136.172.23", "8051", enable_flush_data=False, log_level=6)
        product_id = "1903"
        enable_flush_data = False
        client = AsrClient("172.18.53.16", "8050", product_id, enable_flush_data)
        name = threading.currentThread().getName()
        while i < repeat_num:
            i += 1
            response = client.get_result("/Users/xiashuai01/Downloads/10s.wav")
            for res in response:
                logging.info(name + "\t" + str(res.error_code) + "\t" + res.error_message + "\t" + res.start_time + "\t" +
                              res.end_time + "\t" + res.result + "\t" + res.serial_num + "\t" + str(res.completed))


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
    print("use time: " + str(time.time() - start_time))
