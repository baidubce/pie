##各demo说明
注：代码中涉及的asr服务的ip、端口,以及访问权限用户名和密码,请向百度相关人员获取
#### 1、异步执行
    async目录下:
    
    AsyncRecognize
    * 异步识别: 输入一个语音流,会实时返回每一句话识别的结果（在指定时间最后音频流识别没有返回就结束）
    * 使用场景: 用于对实时性要求较高的场景,如会议记录
    
    AsyncRecognizeWithStream.java
    * 异步识别: 输入一个语音流,会实时返回每一句话识别的结果（等待所有音频识别完成才结束）
     * 使用场景: 用于对实时性要求较高的场景,如会议记录
    
    AsyncRecognizeWithStreamAndMetaData.java
    * 异步识别: 会准实时返回每个句子的结果.输入一个语音流以及自定义RequestMetaData对象,用来控制请求时候的数据发送速度等参数
    * 使用场景: 用于对实时性要求较高的场景,如会议记录
    
#### 2、同步执行
    sync目录下:
    
    SyncRecognizeWithFileAndMetaData.java
    * 同步识别: 通过传入音频文件以及RequestMetaData对象,用来控制请求时候的数据发送速度等参数.
    *          识别开始后线程会进入等待,直到识别完毕,一次性返回所有结果.
    * 使用场景: 通常用于对实时性要求不高的场景，如离线语音分析
    
    SyncRecognizeWithStreamAndMetaData
    * 同步识别: 输入一个音频文件,线程会进入等待,直到识别完毕,一次性返回所有结果
    * 使用场景: 通常用于对实时性要求不高的场景,如离线语音分析