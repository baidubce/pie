using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using Com.Baidu.Acu.Pie;

namespace ASRStreamingSdk
{
    class AsrClientDemo
    {
        private void Log(string s, params object[] args)
        {
            Console.WriteLine(string.Format(s, args));
        }

        private void Log(string s)
        {
            Console.WriteLine(s);
        }

        public async Task FileAsrAsync(string fileName)
        {
            var client = new AsrClient("180.76.107.131:8213", "1903");
            client.LogLevel = 0;
            Log("Create client");
            var stream = client.NewStream();
            Log("Create new stream");

            FileStream file = new FileStream(fileName, FileMode.Open, FileAccess.Read);
            BinaryReader reader = new BinaryReader(file);

            var responseReaderTask = Task.Run(async () =>
            {
                Log("Begin read from server");
                while (await stream.MoveNext())
                {
                    var response = stream.Current();
                    if (response.ErrorCode == 0)
                    {
                        Log("{0}: result={1} start_time={2}, end_time={3}", response.SerialNum, response.Result, response.StartTime, response.EndTime);
                    }
                    else
                    {
                        Log("Fail: error_code={0}, error_message={1}", response.ErrorCode, response.ErrorMessage);
                    }
                }
                Log("Read complete");
            });

            while (true)
            {
                var bytes = reader.ReadBytes(client.RecommendPacketSize);
                if (bytes.Length == 0)
                {
                    await stream.WriteComplete();
                    Console.WriteLine("Write done");
                    break;
                }
                await stream.Write(bytes);
                Console.WriteLine("Write one fragment with size={0}", client.RecommendPacketSize);
            }

            await responseReaderTask;
            reader.Close();
            file.Close();
            Log("demo complete");

        }

        static void Main(string[] args)
        {
            AsrClientDemo demo = new AsrClientDemo();
            demo.FileAsrAsync("./your_real_wav_or_pcm").Wait();
            Console.ReadLine();
        }
    }
}
