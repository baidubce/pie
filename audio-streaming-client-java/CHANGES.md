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