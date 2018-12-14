from setuptools import setup, find_packages


setup(
    name="grpc-python-client",
    version="1.0.0",
    description="for asr grpc python client",
    author="Baidu gRPC",
    url="www.baidu.com",
    author_email="",
    packages=find_packages(),
    license="ABC",
    python_requires=">=2.7",
    install_requires=["grpcio>=1.1.3"],
    entry_points={
        'console_scripts': [
            'protoc-gen-python_grpc=grpclib.plugin.main:main',
        ],
    },
    classifiers=[
        "Topic :: Utilities",
        "Intended Audience :: Developers",
        "Intended Audience :: Information Technology",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 2.7",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.4",
        "Programming Language :: Python :: 3.5",
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7"],
)
