from setuptools import setup, find_packages
import os

# here = os.path.abspath(os.path.dirname(__file__))
# changes = open(os.path.join(here, 'CHANGES.md')).read()
# with open('CHANGES.md') as f:
#     changes = f.read()

setup(
    name="baidu-acu-asr",
    version="1.2.0",
    description="asr grpc client",
    long_description="[https://github.com/baidubce/pie/tree/master/audio-streaming-client-python]" +
                     "(https://github.com/baidubce/pie/tree/master/audio-streaming-client-python)",
    long_description_content_type='text/markdown',
    author="Baidu",
    url="https://github.com/baidubce/pie/tree/master/audio-streaming-client-python-sdk",
    author_email="1908131339@qq.com",
    packages=find_packages(),
    license="Apache License",
    python_requires=">=2.7",
    install_requires=["protobuf", "grpcio", 'threadpool'],
    keywords=['baidu', 'asr', 'speech'],
)
