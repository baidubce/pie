#include "audio_streaming_client.h"

#include <fstream>
#include <thread>
#include <sstream>
#include <grpcpp/channel.h>
#include <grpcpp/create_channel.h>
#include <grpcpp/client_context.h>
#include <grpcpp/impl/codegen/sync_stream.h>
#include "base64.h"

DEFINE_string(default_app_name, "my_sdk", "SKD's name");
DEFINE_bool(default_enable_long_speech, true, "enable long speech");
DEFINE_bool(default_enable_chunk, true, "enable chunk");
DEFINE_uint32(default_log_level, 4, "log level");
DEFINE_double(default_send_per_second, 0.02, "send per second");
DEFINE_double(default_sleep_ratio, 1, "sleep ratio");
DEFINE_int32(default_timeout, 100, "timeout");
DEFINE_uint32(default_send_package_size, 320, "default bytes send to server");

namespace com {
namespace baidu {
namespace acu {
namespace pie {

int ProductMap::init() {
    set("1903", 8000, "客服模型");
    set("1904", 8000, "客服模型：旅游领域");
    set("1905", 8000, "客服模型：股票领域");
    set("1906", 8000, "客服模型：金融领域");
    set("1907", 8000, "客服模型：能源领域");
    set("888", 16000, "输入法模型");
    set("1888", 16000, "远场模型");
    return 0;
}

int ProductMap::set(const std::string& product_id, unsigned long sample_rate, const std::string& product_name) {
    ProductRecord record;
    record.sample_rate = sample_rate;
    record.name = product_name;
    _product_records[product_id] = record;
    return 0;
}

ProductRecord ProductMap::get(const std::string& product_id) const {
    ProductRecord record;
    auto it = _product_records.find(product_id);
    if (it != _product_records.end()) {
        return it->second;
    } else {
	std::cout << "[error] product id not found" << std::endl; 
	return record;      
    }
}

AsrClient::AsrClient()
	: _set_enable_flush_data(false)
	, _set_product_id(false)
	, _inited(false)
        , _send_package_size(FLAGS_default_send_package_size) {
    _init_request.set_app_name(FLAGS_default_app_name);
    _init_request.set_enable_long_speech(FLAGS_default_enable_long_speech);
    _init_request.set_enable_chunk(FLAGS_default_enable_chunk);
    _init_request.set_log_level(FLAGS_default_log_level);
    _init_request.set_send_per_seconds(FLAGS_default_send_per_second);
    _init_request.set_sleep_ratio(FLAGS_default_sleep_ratio);
    _product_map.init();
}

void AsrClient::set_app_name(const std::string& app_name) {
    _init_request.set_app_name(app_name);
}

void AsrClient::set_enable_flush_data(bool enable_flush_data) {
    _init_request.set_enable_flush_data(enable_flush_data);
    _set_enable_flush_data = true;
}

void AsrClient::set_enable_long_speech(bool enable_long_speech){
    _init_request.set_enable_long_speech(enable_long_speech);
}

void AsrClient::set_enable_chunk(bool enable_chunk) {
    _init_request.set_enable_chunk(enable_chunk);
}

void AsrClient::set_log_level(int log_level) {
    _init_request.set_log_level(log_level);
}

void AsrClient::set_send_per_seconds(double send_per_seconds) {
    _init_request.set_send_per_seconds(send_per_seconds);
}

void AsrClient::set_sleep_ratio(double sleep_ratio) {
    _init_request.set_sleep_ratio(sleep_ratio);
}

void AsrClient::set_product_id(const std::string& product_id) {
    _init_request.set_product_id(product_id);
    _send_package_size = _init_request.send_per_seconds() * _product_map.get(product_id).sample_rate * 2;
    _set_product_id = true;
}

int AsrClient::init(const std::string& address) {
    if (!_set_product_id || !_set_enable_flush_data) {
	std::cerr << "Missing required field `product_id` or `enable_flush_data`" << std::endl;
	return -1;
    }

    _channel = grpc::CreateChannel(address, grpc::InsecureChannelCredentials());
    _inited = true;
    
    return 0;
}

unsigned int AsrClient::get_send_package_size() const {
    return _send_package_size;
}

AsrStream* AsrClient::get_stream() {
    AsrStream* asr_stream = new AsrStream();
    asr_stream->_stub = AsrService::NewStub(_channel);
    if (!asr_stream->_stub) {
        std::cerr << "[error] Fail to create stub in get_stream" << std::endl;
        return NULL;
    } else {
        std::cout << "[debug] Create stub success in get_stream" << std::endl;
    }
    asr_stream->_context.set_deadline(std::chrono::system_clock::now() + std::chrono::seconds(FLAGS_default_timeout));
    asr_stream->_context.AddMetadata("audio_meta", base64_encode(_init_request.SerializeAsString()));
    asr_stream->_stream = asr_stream->_stub->send(&(asr_stream->_context));
    if (!asr_stream->_stream) {
        std::cerr << "[error] Fail to create stream in get_stream" << std::endl;
        return NULL;
    } else {
        std::cout << "[debug] Create stream success in get_stream" << std::endl;	    
    }

    return asr_stream;
}

int AsrClient::destroy_stream(AsrStream* stream) {
    int status = stream->finish();
    delete stream;
    return status;
}

AsrStream::AsrStream()
          : _stream(nullptr)
	  , _stub(nullptr)
          , _writesdone(false) {}

int AsrStream::write(const void* buffer, size_t size, bool is_last) {
    if (_writesdone) {
        std::cerr << "[error] write stream has been done" << std::endl;
	return -1;
    }
    int status = 0;
    int write_return_status = 0;
    if (size < 0) {
        std::cerr << "[error] size < 0 in  write stream" << std::endl;
        status = -1;
    } else {
    	com::baidu::acu::pie::AudioFragmentRequest request;
    	request.set_audio_data(buffer, size);
    	//std::cout << "[debug] will run _stream->Write(request) ... ..." << std::endl;
    	write_return_status = _stream->Write(request);
	if (!write_return_status) {
	    std::cerr << "[error] Write to stream error, status = " << write_return_status << std::endl;
    	    status = -1;
	}
    }
    if (is_last) {
        std::cout << "[debug] will run _stream->WritesDone" << std::endl;
	write_return_status = _stream->WritesDone();
	if (write_return_status) {
	    std::cout << "[info] Write done" << std::endl;
	    _writesdone = true;
	} else {
	    std::cerr << "[error] Write done in stream error, status = " << write_return_status << std::endl;
	    status = -1;
	}
    }
    return status;
}

int AsrStream::read(AsrStreamCallBack callback_fun, void* data) {
    AudioFragmentResponse response;
    //std::cout << "[debug] will run _stream->Read(&response) ... ..." << std::endl;
    int read_return_status = 0;
    read_return_status = _stream->Read(&response);
    if (read_return_status) {
        //std::cout << "[debug] run callback_fun ... ..." << std::endl;
	callback_fun(response, data);
        return 0;
    } else {
        std::cout << "[debug] _stream->Read return false, status = " << read_return_status << std::endl;
        return -1;
    }
}

int AsrStream::finish() {
    grpc::Status status = _stream->Finish();
    if (!status.ok()) {
        std::cerr << "Fail to finish stream when destroy AsrStream" << std::endl;
	return -1;
    }
    std::cout << "Stream finished." << std::endl;
    return 0;
}

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com
