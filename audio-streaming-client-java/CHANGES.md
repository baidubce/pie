# 0.9.3
## fix
- 引入了slf4j-包，避免上层业务忘记引入而导致没日志输出
- 修复了处理识别结果时，由于asr 后端返回的时间格式问题，而导致 parse time 失败的问题。对于无法 parse 的时间，会设成00:00:00。

# 0.9.2
## feature
- 开放了对于 sendPerSeconds 参数的设置
- 现在 sleepRatio 可以小于 1

## improvement
添加了一个批量识别文件夹内音频(不支持递归)，并输出结果到文件的简单 demo

# 0.9.1
## feature
添加了两种新的 Product 类型：远场模型和远场模型-机器人领域

# 0.9.0
## improvement
- 将 com.baidu.acu.pie.model.RecognitionResult中的 startTime 和 endTime 两个字段的类型由 String 修改为 LocalTime