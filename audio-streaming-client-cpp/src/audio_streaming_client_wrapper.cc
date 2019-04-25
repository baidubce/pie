#include "audio_streaming_client.h"
#include "audio_streaming_client_wrapper.h"

using namespace com::baidu::acu::pie;

extern "C" {

AsrClientWrapper* asr_client_create() {
    return new AsrClient();
}
int asr_client_init(AsrClientWrapper* asr_client, char* address) {
    return ((AsrClient*) asr_client)->init(std::string(address));
}
void asr_client_destroy(AsrClientWrapper* asr_client) {
    delete (AsrClient*) asr_client;
}

void asr_client_set_app_name(AsrClientWrapper* asr_client, char* app_name) {
    return ((AsrClient*) asr_client)->set_app_name(std::string(app_name));
}

void asr_client_set_enable_flush_data(AsrClientWrapper* asr_client, bool enable_flush_data) {
    return ((AsrClient*) asr_client)->set_enable_flush_data(enable_flush_data);
}

void asr_client_set_enable_long_speech(AsrClientWrapper* asr_client, bool enable_long_speech) {
    return ((AsrClient*) asr_client)->set_enable_long_speech(enable_long_speech);
}

void asr_client_set_log_level(AsrClientWrapper* asr_client, int log_level) {
    return ((AsrClient*) asr_client)->set_log_level(log_level);
}

void asr_client_set_user_name(AsrClientWrapper* asr_client, char* user_name) {
    return ((AsrClient*) asr_client)->set_user_name(user_name);
}

void asr_client_set_expire_time(AsrClientWrapper* asr_client, char* expire_time) {
    return ((AsrClient*) asr_client)->set_expire_time(expire_time);
}

void asr_client_set_token(AsrClientWrapper* asr_client, char* token) {
    return ((AsrClient*) asr_client)->set_token(token);
}

void asr_client_set_send_per_seconds(AsrClientWrapper* asr_client, double send_per_seconds) {
    return ((AsrClient*) asr_client)->set_send_per_seconds(send_per_seconds);
}

void asr_client_set_sleep_ratio(AsrClientWrapper* asr_client, double sleep_ratio) {
    return ((AsrClient*) asr_client)->set_sleep_ratio(sleep_ratio);
}

void asr_client_set_product_id(AsrClientWrapper* asr_client, char* product_id) {
    return ((AsrClient*) asr_client)->set_product_id(std::string(product_id));
}

unsigned int asr_client_get_send_package_size(AsrClientWrapper* asr_client) {
    return ((AsrClient*) asr_client)->get_send_package_size();
}

AsrStreamWrapper* asr_client_new_stream(AsrClientWrapper* asr_client) {
    return ((AsrClient*) asr_client)->get_stream();
}

int asr_client_destroy_stream(AsrClientWrapper* asr_client, AsrStreamWrapper* stream_wrapper) {
    return ((AsrClient*) asr_client)->destroy_stream((AsrStream*) stream_wrapper);
}

typedef struct {
    void * callback;
    void * data;
} CallbackAdaptor;

void callback_wrapper(AudioFragmentResponse &resp, void* data) {
    CallbackAdaptor* adaptor = (CallbackAdaptor*)data;
    AsrStreamCallBackWrapper callBackWrapper = (AsrStreamCallBackWrapper)(adaptor->callback);
    AudioFragmentResponseWrapper responseWrapper;
    com::baidu::acu::pie::AudioFragmentResult *audio_fragment = resp.mutable_audio_fragment();
    responseWrapper.error_code = resp.error_code();
    responseWrapper.error_message = resp.error_message().data();
    responseWrapper.type = resp.type();
    responseWrapper.audio_fragment.serial_num = audio_fragment->serial_num().data();
    responseWrapper.audio_fragment.start_time = audio_fragment->start_time().data();
    responseWrapper.audio_fragment.end_time = audio_fragment->end_time().data();
    responseWrapper.audio_fragment.result = audio_fragment->result().data();
    responseWrapper.audio_fragment.complete = audio_fragment->completed();
    callBackWrapper(&responseWrapper, adaptor->data);
}

int asr_stream_read(AsrStreamWrapper* stream_wrapper, AsrStreamCallBackWrapper callback, void* data) {
    CallbackAdaptor adaptor;
    adaptor.callback = (void *)callback;
    adaptor.data = data;
    return ((AsrStream*) stream_wrapper)->read(callback_wrapper, &adaptor);
}

int asr_stream_write(AsrStreamWrapper* stream_wrapper, const void* buffer, int size, bool is_last) {
    return ((AsrStream*) stream_wrapper)->write(buffer, size, is_last);
}
}
