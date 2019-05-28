# 1.1.0
## feature
在构建 AsrClient 的时候，可以传入一个 ChannelConfig 实例，用来控制 keepalive 超时相关参数。

# 1.0.0
**这是一次不向下兼容的升级。**

## feature
- 在每次构建 AsrClient 的时候，支持传入用户名和密码，可以做一些 authentication。
- 在每次调用 AsrClient 进行识别的时候，可以传入一个 RequestMeta 对象，对本次识别进行一些调整(如改变发包速度，是否展示中间结果等)

## improvement
- AsrConfig 现在支持 builder 和 标准的 getter/setter。[issue #56](https://github.com/baidubce/pie/issues/56)

## deprecated
- 废弃了 AsrClient 中老的 asyncRecognize 接口。
- 废弃了 AsrConfig 中一些老的方法，请使用新加入的 builder 或者 setter。
- 废弃了 0.9.4 版本中加入的 productId。

# 0.10.0
## improvement
- jdk降级到1.7版本， 同时支持android 7.x版本
- **接口不完全兼容历史版本，asyncRecognize参数的类型有变化**

# 0.9.4
## improvement
- 现在可以直接设置productId，详见 com.baidu.acu.pie.demo.JavaDemo的43行。对于不存在与 AsrProduct 中的模型类型，可以使用 productId 进行更灵活的设置。
- 在 JavaDemo 里添加了一些注释说明

## fix
- 修改了由于发包大小偏小，导致异步流式识别过程中，识别出错中断的问题。
- 修改了依赖策略，将 io.grpc:grpc-stub 和 com.google.protobuf:protobuf-java:3.6.1 两个包暴露到上层，避免上层重复引用。

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