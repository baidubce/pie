#include "audio_streaming_client.h"
#include <unistd.h>
#include <iostream>
#include <sstream>
#include <thread>

void default_callback(const com::baidu::acu::pie::AudioFragmentResponse& resp, void* data) {
        //std::cout << "[debug] do recoging ... ..." << std::endl;
	std::stringstream ss;
	if (data) {
		char* tmp = (char*) data;
		std::cout << "data = " << tmp << std::endl;
	}
	ss << "Receive completed" << resp.completed()
	   << ", start=" << resp.start_time()
	   << ", end=" << resp.end_time()
	   << ", content=" << resp.result();
	std::cout << ss.str() << std::endl;
}

void write_to_stream(com::baidu::acu::pie::AsrClient client, 
		     com::baidu::acu::pie::AsrStream* stream, FILE* fp, int case_count) {
    int size = client.get_send_package_size();
    char buffer[size];
    size_t count = 0;
    while (!std::feof(fp)) {
        count = fread(buffer, 1, size, fp);
        //std::cout << "[debug] write stream " << std::endl;
        if (stream->write(buffer, count, false) != 0) {
            std::cerr << "[error] stream write buffer error" << std::endl;
            break;
        }
        if (count < 0) {
                std::cerr << "[warning] count < 0 !!!!!!!!" << std::endl;
    	break;
        }
        //usleep(150*1000);
    }
    //std::cout << "[debug] write stream " << std::endl;
    stream->write(nullptr, 0, true);
    std::cout << "[debug] count of last buffer=" << count << std::endl;
}

void read_from_stream_once(com::baidu::acu::pie::AsrStream* stream) {
    if (stream->read(default_callback, nullptr) == 0) {
        std::cout << "[debug] read from stream once success" << std::endl;
    } else {
        std::cout << "[debug] read from stream once failed" << std::endl;
    }
}

int main(int argc, char* argv[]) {
	// Parse gflags
	google::ParseCommandLineFlags(&argc, &argv, true);
        size_t case_count = 2;
	std::string audio_file[2];
	audio_file[0] = "../../data/10s.wav";
	audio_file[1] = "../../data/8k-0.pcm";
        com::baidu::acu::pie::AsrClient client;
	client.set_enable_flush_data(true);
	if (argc == 3) {
	    client.set_product_id(argv[1]);
	    client.init(argv[2]);
	} else {
	    client.set_product_id("1906");
	    client.init("10.190.115.11:8200");
	}
	
	for (size_t i = 0; i < case_count; i++) {
	    std::cout << "case " << i << " start..." << std::endl;
	    // do not call stream->write after sending the last buffer
            com::baidu::acu::pie::AsrStream* stream = client.get_stream();
	    std::cout << "client get stream success" << std::endl;

	    std::thread readbeforewrite(read_from_stream_once, stream);

            // test : start read before write
	    //if (readbeforewrite.joinable()) {
            //    readbeforewrite.join();
            //}

	    FILE* fp = fopen(audio_file[i].c_str(), "rb");
            if (!fp) {
                    std::cerr << "Failed to open file=" << audio_file[i] << std::endl;
                    return -1;
            }
            std::cout << "Stream : Open file=" << audio_file[i] << std::endl;
    
	    std::thread writer(write_to_stream, client, stream, fp, case_count);

	    // test : start read before write
            if (readbeforewrite.joinable()) {
                readbeforewrite.join();
            }
	    
	    char tmp[100] = "\0";
	    sprintf(tmp,"case %d.\0",i);
	    std::cout << "Start to read " << tmp << std::endl;
    	    int read_num = 1;
	    while (stream->read(default_callback, tmp) == 0) {
    	        //std::cout << "[debug] read stream return " << read_num++ << "times" << std::endl;
		usleep(150*1000);
    	    }
	    std::cout << "Complete to read " << tmp << std::endl;
            
	    if (writer.joinable()) {
	        writer.join();
	    }
	    std::cout << "case " << i << " complete" << std::endl;
	   
	    if (client.destroy_stream(stream) != 0) {
	        std::cerr << "[error] client destroy stream failed" << std::endl;
	    } else {
	        std::cout << "client destroy stream success" << std::endl;
	    }
	}
       
        return 0;
}

