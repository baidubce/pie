#include "audio_streaming_client_wrapper.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include "sha256.h"

void default_callback(AudioFragmentResponseWrapper* resp, void* data) {
    if (resp->error_code != 0) {
        printf("Server error, error_code=%d error_message=%s\n", resp->error_code, resp->error_message);
    } else {
        if(resp->type == FRAGMENT_DATA) {
            printf("Receive response from server, serial_num=%s, complete=%d, start=%s, end=%s, content=%s\n",
               resp->audio_fragment.serial_num, resp->audio_fragment.complete, resp->audio_fragment.start_time,
               resp->audio_fragment.end_time, resp->audio_fragment.result);
        } else {
            printf("error resp type is : %d", resp->type);
        }
    }
}

typedef struct {
    AsrStreamWrapper* stream;
    FILE* fp;
    unsigned int send_package_size;
} WriteArguments;

void* write_to_stream(void* args) {
    WriteArguments* arguments = (WriteArguments*)args;
    AsrStreamWrapper* stream = arguments->stream;
    FILE* fp = arguments->fp;
    int send_package_size = arguments->send_package_size;

    char buffer[send_package_size];
    size_t count = 0;
    while (!feof(fp)) {
        count = fread(buffer, 1, send_package_size, fp);
        if (count < 0) {
            printf("Read file done\n");
            break;
        }
        if (asr_stream_write(stream, buffer, count, false) != 0) {
            printf("Fail to write stream\n");
            break;
        }
        printf("Write one piece to server with size=%d\n", count);

        // you can add usleep to simulate audio pause
        //usleep(150*1000);
    }
    asr_stream_write(stream, NULL, 0, true);
    printf("Write stream done\n");

    return NULL;
}

void gen_hash(char* in, char * out)
{
    int idx;
    unsigned char hash[32];
    SHA256_CTX ctx;
    sha256_init(&ctx);
    sha256_update(&ctx, (unsigned char*)in, strlen(in));
    sha256_final(&ctx,hash);
    for (idx=0; idx < 32; idx++) {
      sprintf(out,"%02x",hash[idx]);
      out = out+2;
   }
}

int main(int argc, char* argv[]) {
    size_t case_count = 2;

    // specify audio path
    char* audio_file = "../../data/10s.wav";

    // create AsrClient
    AsrClientWrapper* client = asr_client_create();

    asr_client_set_enable_flush_data(client, true);
    if (argc == 7) {
        asr_client_set_product_id(client, argv[1]);
        if (asr_client_init(client, argv[2], atoi(argv[6])) != 0) {
            printf("Init asr client failed\n");
            return -1;
        }
        asr_client_set_user_name(client, argv[3]);
        asr_client_set_expire_time(client, argv[5]);
        char str[512] = {0};
        char token[512] = {0};
        strcat(str, argv[3]); //user_name
        strcat(str, argv[4]); //password
        strcat(str, argv[5]); //expire_time UTC format, 2019-04-25T12:41:16Z
        gen_hash(str, token);
        asr_client_set_token(client, (char*)token);
    } else {
        asr_client_set_product_id(client, "1906");
        if (asr_client_init(client, "127.0.0.1:8200", 0)) {
            printf("Init asr client failed\n");
            return -1;
        }
    }

    // do not call stream->write after sending the last buffer
    AsrStreamWrapper* stream_wrapper = asr_client_new_stream(client);

    printf("client get stream success\n");
    FILE* fp = fopen(audio_file, "rb");
    if (!fp) {
        printf("Failed open file name=%s", audio_file);
        return -1;
    }
    printf("Open file=%s\n", audio_file);

    pthread_t writer;
    WriteArguments arguments;
    arguments.stream = stream_wrapper;
    arguments.fp = fp;
    arguments.send_package_size = asr_client_get_send_package_size(client);

    pthread_create(
            &writer,
            NULL,
            write_to_stream,
            &arguments);

    // read from stream continuously
    printf("Start to read\n");
    while (asr_stream_read(stream_wrapper, default_callback, NULL) == 0) {
    }

    printf("Read complete\n");
    pthread_join(writer, NULL);

    if (asr_client_destroy_stream(client, stream_wrapper) != 0) {
        printf("Failed to destroy stream\n");
    } else {
        printf("Destroy stream success\n");
    }
    asr_client_destroy(client);
    return 0;
}

