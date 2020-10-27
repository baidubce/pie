# JS 客户端
### 模块描述
功能按script标签分模块：

- 第一个script标签内均为工具函数，用于格式转换，转换为接口需要的格式
- 第二个script标签内为实时录音及通过websocket发送音频流
- 第三个script标签内为上传文件识别逻辑，流式识别将整个音频流分为多块流式发送

### 常用参数
- WS_URL：配置 websocket请求url
- DEFAULT_PARAMS：配置默认参数
- onMessage：接收识别结果，实时识别支持每个字的变化过程可视化
