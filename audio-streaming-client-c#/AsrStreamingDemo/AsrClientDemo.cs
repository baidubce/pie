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

        public async Task FileAsrAsync(AsrStream stream, string fileName, int packageSize)
        {

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
                var bytes = reader.ReadBytes(packageSize);
                if (bytes.Length == 0)
                {
                    await stream.WriteComplete();
                    Log("Write done");
                    break;
                }
                await stream.Write(bytes);
                Log("Write one fragment with size={0}", packageSize);
            }

            await responseReaderTask;
            reader.Close();
            file.Close();
            Log("demo complete");
        }

        static void Main(string[] args)
        {
            var client = new AsrClient("10.190.115.11:8200", "1903");
            client.LogLevel = 0;
            Console.WriteLine("Create client");
            AsrClientDemo demo = new AsrClientDemo();

            var stream0 = client.NewStream();
            var task0 = demo.FileAsrAsync(stream0, "C:\\Users\\sunyumei\\Documents\\GitHub\\pie\\zhiyu1.wav", client.RecommendPacketSize);

            var stream1 = client.NewStream();
            var task1 = demo.FileAsrAsync(stream1, "C:\\Users\\sunyumei\\Documents\\GitHub\\pie\\zhiyu2.wav", client.RecommendPacketSize);

            task0.Wait();
            task1.Wait();
            Console.ReadLine();
        }
    }
}
