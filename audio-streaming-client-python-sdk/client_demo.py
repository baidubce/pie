# -*-coding:utf-8-*-
from baidu_acu_asr.AsrClient import AsrClient
import threading

client = AsrClient("172.18.53.17", "31051", enable_flush_data=False)


def run():
    response = client.get_result("/Users/xiashuai01/Downloads/300s.wav")
    for res in response:
        print("start_time\tend_time\tresult")
        print(res.start_time + "\t" + res.end_time + "\t" + res.result)


if __name__ == '__main__':
    run()
    # 多线程运行
    # for i in range(100):
    #     print(i)
    #     t = threading.Thread(target=run, args=[])
    #     t.start()



