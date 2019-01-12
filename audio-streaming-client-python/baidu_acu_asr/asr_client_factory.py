from asr_client import AsrClient


class AsrClientFactory(object):

    def __init__(self):
        pass

    @staticmethod
    def get_8k_instance(url, port, product_id, enable_flush_data):
        return AsrClient(url, port, product_id, enable_flush_data,
                         send_per_seconds=0.16, send_package_size=2560)

    @staticmethod
    def get_16k_instance(url, port, product_id, enable_flush_data):
        return AsrClient(url, port, product_id, enable_flush_data,
                         send_per_seconds=0.01, send_package_size=320)
