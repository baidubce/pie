### 版本及依赖
客户端会以 jar 包的形式发布到 maven 仓库中，

**使用要求：**

 - 支持 jdk1.7及以上版本
 - 使用 gradle 或 maven 构建项目

**使用方式**

**gradle项目**
添加依赖：

```
dependencies {
	compile "com.baidu.acu.pie:audio-streaming-client-java:1.1.12"
}
```

**maven项目**
添加依赖：

```

<dependencies>
	<dependency>
	  <groupId>com.baidu.acu.pie</groupId>
	  <artifactId>audio-streaming-client-java</artifactId>
	  <version>1.1.10</version>
	</dependency>
</dependencies>
```

**更多使用样例，请参考 https://github.com/baidubce/pie/tree/master/java-demo** 

## 快速开始
### 构建（会自动生成 grpc 代码）
`mvn clean compile`

### 生成grpc代码

`mvn protobuf:compile`

`mvn protobuf:compile-custom`


## 如何贡献
TODO

### 发布至bintray
`mvn clean deploy`

请联系作者，获取发布配置

## 讨论
TODO

