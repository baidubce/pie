from setuptools import setup, find_packages


setup(
    name="baidu-acu-asr",
    version="1.0.3",
    description="asr grpc client",
    long_description="this is an asr grpc client",
    author="Baidu",
    url="https://github.com/baidubce/pie/tree/master/audio-streaming-client-python-sdk",
    author_email="1908131339@qq.com",
    packages=find_packages(),
    license="Apache License",
    python_requires=">=2.7",
    install_requires=["protobuf", "grpcio"],
    keywords = ['baidu', 'asr', 'speech'],
)
