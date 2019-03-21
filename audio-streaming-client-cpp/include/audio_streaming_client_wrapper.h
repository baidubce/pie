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
int asr_client_init(AsrClientWrapper*, char* address);
void asr_client_destroy(AsrClientWrapper*);

void asr_client_set_app_name(AsrClientWrapper* asr_client, char* app_name);
void asr_client_set_enable_flush_data(AsrClientWrapper* asr_client, bool enable_flush_data);
void asr_client_set_enable_long_speech(AsrClientWrapper* asr_client, bool enable_long_speech);
void asr_client_set_log_level(AsrClientWrapper* asr_client, int log_level);
void asr_client_set_send_per_seconds(AsrClientWrapper* asr_client, double send_per_seconds);
void asr_client_set_sleep_retio(AsrClientWrapper* asr_client, double sleep_ratio);
void asr_client_set_product_id(AsrClientWrapper*, char* product_id);
void asr_client_set_flush_data(AsrClientWrapper*, bool enable_flush);
unsigned int asr_client_get_send_package_size(AsrClientWrapper* asr_client);

AsrStreamWrapper* asr_client_new_stream(AsrClientWrapper*);
int asr_client_destroy_stream(AsrClientWrapper*, AsrStreamWrapper*);

int asr_stream_read(AsrStreamWrapper*, AsrStreamCallBackWrapper, void* data);
int asr_stream_write(AsrStreamWrapper*, const void* buffer, int size, bool is_last);

#ifdef __cplusplus
}
#endif

#endif //AUDIO_STREAMING_CLIENT_CPP_AUDIO_STREAMING_CLIENT_WRAPPER_H
