# Web 客户端

### 常用参数
- WS_URL：配置 websocket请求url
- DEFAULT_PARAMS：配置默认参数
- onMessage：接收识别结果，实时识别支持每个字的变化过程可视化

### 注意事项
上传文件识别音频流发送间隔0-18ms，会导致识别延迟，2-3分钟才会出识别结果，19ms以上识别不会出现延迟问题，在这里用 INTERVAL 配置。
