//
// Created by 何建斌 on 2019/3/7.
//

#ifndef AUDIO_STREAMING_CLIENT_CPP_AUDIO_STREAMING_CLIENT_WRAPPER_H
#define AUDIO_STREAMING_CLIENT_CPP_AUDIO_STREAMING_CLIENT_WRAPPER_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>

typedef struct {
    int error_code;
    const char* error_message;
    const char* start_time;
    const char* end_time;
    const char* result;
    bool complete;
    const char* serial_num;
} AudioFragmentResponseWrapper;
typedef void AsrClientWrapper;
typedef void AsrStreamWrapper;
typedef void (* AsrStreamCallBackWrapper)(AudioFragmentResponseWrapper* resp, void* data);

AsrClientWrapper* asr_client_create();
// 初始化：指定asr流式服务器的地址和端口，私有化版本请咨询供应商
int asr_client_init(AsrClientWrapper*, char* address);
void asr_client_destroy(AsrClientWrapper*);

// asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
void asr_client_set_app_name(AsrClientWrapper* asr_client, char* app_name);
// 是否返回中间翻译结果
void asr_client_set_enable_flush_data(AsrClientWrapper* asr_client, bool enable_flush_data);
// 是否允许长音频
void asr_client_set_enable_long_speech(AsrClientWrapper* asr_client, bool enable_long_speech);
// 服务端的日志输出级别
void asr_client_set_log_level(AsrClientWrapper* asr_client, int log_level);
// 指定每次发送的音频数据包大小，通常不需要修改
void asr_client_set_send_per_seconds(AsrClientWrapper* asr_client, double send_per_seconds);
// 指定asr服务的识别间隔，通常不需要修改，不能小于1
void asr_client_set_sleep_ratio(AsrClientWrapper* asr_client, double sleep_ratio);
// asr识别服务的产品类型，私有化版本请咨询供应商
void asr_client_set_product_id(AsrClientWrapper*, char* product_id);
// 获取每次发送的音频字节
unsigned int asr_client_get_send_package_size(AsrClientWrapper* asr_client);

// 创建语音流
AsrStreamWrapper* asr_client_new_stream(AsrClientWrapper*);
// 销毁语音流
int asr_client_destroy_stream(AsrClientWrapper*, AsrStreamWrapper*);

// 读取语音流中数据，该方法为阻塞型方法，语音流中有识别结果后才会返回
int asr_stream_read(AsrStreamWrapper*, AsrStreamCallBackWrapper, void* data);
// 向语音流中写数据，需要指定是否是最后一块音频，默认不是最后一块
int asr_stream_write(AsrStreamWrapper*, const void* buffer, int size, bool is_last);

#ifdef __cplusplus
}
#endif

#endif //AUDIO_STREAMING_CLIENT_CPP_AUDIO_STREAMING_CLIENT_WRAPPER_H
