#ifndef AUDIO_STREAMING_CLIENT_CPP_LIBRARY_H
#define AUDIO_STREAMING_CLIENT_CPP_LIBRARY_H

#include "audio_streaming.grpc.pb.h"
#include <algorithm>
#include <gflags/gflags.h>
#include <grpc/grpc.h>

namespace com {
namespace baidu {
namespace acu {
namespace pie {

typedef void (*AsrClientCallBack) (const AudioFragmentResponse& resp, void* data);

class AsrClient {
public:
	AsrClient();
	void set_app_name(const std::string& app_name);
	void set_enable_flush_data(bool enable_flush_data);
	void set_enable_long_speech(bool enable_long_speech);
	void set_enable_chunk(bool enable_chunk);
	void set_log_level(int log_level);
	void set_send_per_seconds(double send_per_seconds);
	void set_sleep_ratio(double sleep_raio);
	void set_product_id(const std::string& product_id);
    int init(const std::string& address);
    int send_audio(const std::string& audio_file, AsrClientCallBack callback);

private:
    grpc::ClientContext _context;
	std::shared_ptr<grpc::Channel> _channel;
	InitRequest _init_request;
	bool _set_enable_flush_data;
	bool _set_product_id;
	bool _inited;
};

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com

#endif
