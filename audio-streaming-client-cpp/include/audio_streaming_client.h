#ifndef AUDIO_STREAMING_CLIENT_CPP_LIBRARY_H
#define AUDIO_STREAMING_CLIENT_CPP_LIBRARY_H

#include "audio_streaming.grpc.pb.h"
#include <algorithm>
#include <gflags/gflags.h>
#include <grpc/grpc.h>

DECLARE_string(app_name);
DECLARE_bool(enable_flush_data);
DECLARE_bool(enable_long_speech);
DECLARE_bool(enable_chunk);
DECLARE_uint32(log_level);
DECLARE_double(send_per_second);
DECLARE_double(sleep_ratio);
DECLARE_string(product_id);

namespace com {
namespace baidu {
namespace acu {
namespace pie {

typedef void (*AsrClientCallBack) (const AudioFragmentResponse& resp, void* data);

class AsrClient {
public:
    int init(const std::string& address);
    int send_audio(const std::string& audio_file);
    void set_response_callback(AsrClientCallBack callback_fun);

private:
    std::unique_ptr<AsrService::Stub> _stub;
    grpc::ClientContext _context;
    AsrClientCallBack _callback_fun;
};

} // namespace pie
} // namespace acu
} // namespace baidu
} // namespace com

#endif