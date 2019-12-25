# -*-coding:utf-8-*-
import threading
from baidu_acu_asr.asr_client import AsrClient
import os
import time
import logging
import threadpool
import baidu_acu_asr.audio_streaming_pb2
from baidu_acu_asr.asr_product import AsrProduct


class AudioHandler:

    def __init__(self):
        pass

    logging.basicConfig(filename="asr_result.log")
    url = "127.0.0.1"
    port = "8050"
    log_level = 0
    product_id = "888"
    sample_rate = 16000
    product = AsrProduct.CUSTOMER_SERVICE_FINANCE
    enable_flush_data = False
    user_name = "abc"
    password = "123"

    def write_file(self, file_path, file_content):
        with open(file_path, "w") as file:
            file.write(file_content.encode("UTF-8"))

    def get_audio_files(self, path):
        audio_names = os.listdir(path)
        audio_paths = []
        for audio_name in audio_names:
            if audio_name.endswith(".wav") or audio_name.endswith(".pcm"):
                logging.info(os.path.join(path, audio_name))
                audio_paths.append(os.path.join(path, audio_name))
        return audio_paths

    def run(self, file_path):
        while True:
            client = AsrClient(self.url, self.port, None, self.enable_flush_data,
                               product_id=self.product_id,
                               sample_rate=self.sample_rate,
                               log_level=self.log_level,
                               user_name=self.user_name,
                               password=self.password)
            responses = client.get_result(file_path)
            file_content = ""
            try:
                for response in responses:
                    if response.type == baidu_acu_asr.audio_streaming_pb2.FRAGMENT_DATA:
                        file_content += response.audio_fragment.result
                        logging.info(file_content)
                    else:
                        logging.warning("type is: %d", response.type)
                self.write_file(file_path + ".txt", file_content)
                logging.info("file %s write complete!", file_path)
                break
            except Exception as ex:
                # 如果出现异常，此处需要重试当前音频
                logging.error("encounter an error: %s, will create a new channel and retry audio!", ex.message)
                time.sleep(0.5)


if __name__ == '__main__':
    start_time = time.time()
    handler = AudioHandler()

    audio_path = "./testaudio"
    audio_files = handler.get_audio_files(audio_path)
    pool = threadpool.ThreadPool(10)
    requests = threadpool.makeRequests(handler.run, audio_files)
    [pool.putRequest(req) for req in requests]
    pool.wait()
    logging.info("complete! use time: %s", str(time.time() - start_time))

