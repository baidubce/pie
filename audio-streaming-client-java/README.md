### 版本及依赖
客户端会以 jar 包的形式发布到 maven 仓库中，

**使用要求：**

 - 支持 jdk1.7及以上版本
 - 使用 gradle 或 maven 构建项目

**使用方式**

**gradle项目**

添加 jcenter 到 repository 中，并添加依赖：

```
repositories {
    jcenter()
}
dependencies {
	compile "com.baidu.acu.pie:audio-streaming-client-java:1.0.0.SNAPSHOT"
}
```

**maven项目**

同样添加 jcenter 到 repository 中，并添加依赖：

```
<repositories>
    <repository>
       <id>jcenter</id>
       <url>https://jcenter.bintray.com</url>
    </repository>
</repositories>

<dependencies>
	<dependency>
	  <groupId>com.baidu.acu.pie</groupId>
	  <artifactId>audio-streaming-client-java</artifactId>
	  <version>0.10.1.SNAPSHOT</version>
	</dependency>
</dependencies>
```

## 快速开始
### 构建（会自动生成 grpc 代码）
`./gradlew build`

### 生成grpc代码
`./gradlew generateProto`

## 如何贡献
TODO

### 发布至bintray
`./gradlew bintrayUpload -Puser=<username> -Pkey=<appkey>`

请联系作者，获取 username 和 key

## 讨论
TODO

