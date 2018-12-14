//
// Created by Sun,Weicheng on 2018/12/13.
//

#include "audio_streaming_client.h"

int main(int argc, char* argv[]) {
	// Parse gflags
	google::ParseCommandLineFlags(&argc, &argv, true);
    com::baidu::acu::pie::AsrClient client;
    client.init("172.18.53.17:31051");
	client.send_audio("10s.wav");
}
