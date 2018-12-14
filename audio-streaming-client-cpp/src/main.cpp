//
// Created by Sun,Weicheng on 2018/12/13.
//

#include "audio_streaming_client.h"
#include <sstream>
#include <iostream>

void default_callback(const com::baidu::acu::pie::AudioFragmentResponse& resp, void*) {
	std::stringstream ss;
	ss << "Receive completed" << resp.completed()
	   << ", start=" << resp.start_time()
	   << ", end=" << resp.end_time()
	   << ", content=" << resp.result();
	std::cout << ss.str() << std::endl;
}

int main(int argc, char* argv[]) {
	// Parse gflags
	google::ParseCommandLineFlags(&argc, &argv, true);
    com::baidu::acu::pie::AsrClient client;
	client.set_enable_flush_data(true);
	client.set_enable_long_speech(true);
	client.set_enable_chunk(true);
	client.set_log_level(5);
	client.set_send_per_seconds(0.16);
	client.set_sleep_ratio(1);
	client.set_product_id("1903");
    client.init("180.76.107.131:8051");
	client.send_audio("10s.wav", default_callback);
}
