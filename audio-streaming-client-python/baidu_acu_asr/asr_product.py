# -*- coding: utf-8 -*-
from enum import Enum


class ProductMap(object):

    map = {
        "1903": [8000, "客服模型"],
        "1904": [8000, "客服模型：旅游领域"],
        "1905": [8000, "客服模型：股票领域"],
        "1906": [8000, "客服模型：金融领域"],
        "1907": [8000, "客服模型：能源领域"],
        "888": [16000, "输入法模型"],
        "1888": [16000, "远场模型"],
        "1889": [16000, "远场模型-机器人领域"],
        "1": [16000, "远场模型-法院用"]
    }

    @classmethod
    def get(cls, product_id):
        return cls.map[product_id][0]


class AsrProduct(Enum):
    CUSTOMER_SERVICE = ["客服模型", "1903", 8000]
    CUSTOMER_SERVICE_TOUR = ["客服模型：旅游领域", "1904", 8000]
    CUSTOMER_SERVICE_STOCK = ["客服模型：股票领域", "1905", 8000]
    CUSTOMER_SERVICE_FINANCE = ["客服模型：金融领域", "1906", 8000]
    CUSTOMER_SERVICE_ENERGY = ["客服模型：能源领域", "1907", 8000]
    INPUT_METHOD = ["输入法模型", "888", 16000]
    FAR_FIELD = ["远场模型", "1888", 16000]
    FAR_FIELD_ROBOT = ["远场模型：机器人领域", "1889", 16000]
    CHONGQING_FAYUAN = ["重庆高院", "1", 16000]