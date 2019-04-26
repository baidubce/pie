#ifndef ACU_PIAT_UNIMRCP_AUDIO_STREAMING_CLIENT_H
#define ACU_PIAT_UNIMRCP_AUDIO_STREAMING_CLIENT_H

#include "audio_streaming.grpc.pb.h"

#include <algorithm>
#include <gflags/gflags.h>
#include <grpc/grpc.h>
#include <map>
#include <string>

namespace com {
namespace baidu {
namespace acu {
namespace pie {

typedef void (*AsrStreamCallBack) (AudioFragmentResponse& resp, void* data);

class AsrStream;

struct ProductRecord {
    ProductRecord()
        : sample_rate(0){
    }

    unsigned long sample_rate;
    std::string name;
};

class ProductMap {
public:
    int init();
    int set(const std::string& product_id, unsigned long sample_rate, const std::string& product_name);
    ProductRecord get(const std::string& product_id) const;

private:
    std::map<std::string, ProductRecord> _product_records;
};

class AsrClient {
public:
    AsrClient();
    // asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
    void set_app_name(const std::string& app_name);
    // 是否返回中间翻译结果
    void set_enable_flush_data(bool enable_flush_data);
    // 是否允许长音频
    void set_enable_long_speech(bool enable_long_speech);
    void set_enable_chunk(bool enable_chunk);
    // 服务端的日志输出级别
    void set_log_level(int log_level);
    // 指定每次发送的音频数据包大小，通常不需要修改
    void set_send_per_seconds(double send_per_seconds);
    // 指定asr服务的识别间隔，通常不需要修改，不能小于1
    void set_sleep_ratio(double sleep_raio);
    // asr识别服务的产品类型，私有化版本请咨询供应商
    void set_product_id(const std::string& product_id);
    // 用户名
    void set_user_name(const std::string& user_name);
    // 超时时间为UTC格式（），如果小于当前时间，校验失败
    void set_expire_time(const std::string& expire_time);
    // user_name passwd expire_time 用sha256生成的token
    void set_token(const std::string& token);
    // 获取每次发送的音频字节数
    unsigned int get_send_package_size() const;
    // 初始化：指定asr流式服务器的地址和端口，私有化版本请咨询供应商
    int init(const std::string& address);
    // 创建语音流
    AsrStream* get_stream();
    // 销毁语音流
    int destroy_stream(AsrStream* stream);
private:
    std::shared_ptr<grpc::Channel> _channel;
    InitRequest _init_request;
    ProductMap _product_map;
    unsigned int _send_package_size;
    bool _set_enable_flush_data;
    bool _set_product_id;
    bool _inited;
};

class AsrStream {
public:
    friend class AsrClient;
    // 读取语音流中数据，该方法为阻塞型方法，语音流中有识别结果后才会返回 
    int read(AsrStreamCallBack callback_fun, void* data);
    // 向语音流中写数据，需要指定是否是最后一块音频，默认不是最后一块
    int write(const void* buffer, size_t size, bool is_last = true);
private:
    int finish();
    AsrStream();
    std::shared_ptr<grpc::ClientReaderWriter<AudioFragmentRequest, AudioFragmentResponse> >  _stream;
    std::unique_ptr<AsrService::Stub> _stub;
    grpc::ClientContext _context;
    bool _writesdone;
};

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com

#endif //ACU_PIAT_UNIMRCP_AUDIO_STREAMING_CLIENT_H
