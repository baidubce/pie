set -x

mypass="sslpie"
targetIP1="192.168.2.2"
targetIP2="192.168.3.3"
domain="www.example-asr.com"

# 生成服务器密钥
openssl genrsa -passout pass:$mypass -des3 -out server.key 4096

# 创建 OpenSSL 配置文件
echo """
[req]
distinguished_name=req
req_extensions = v3_req
prompt = no
[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names
[alt_names]
IP.1 = $targetIP1
IP.2 = $targetIP2
DNS.1 = $domain
""" > openssl_req_config.conf

# 生成服务器签名请求（CSR）
openssl req -passin pass:$mypass -new -key server.key -out server.csr -config openssl_req_config.conf -subj "/C=US/ST=CA/L=Beijing/O=TestVoice/OU=testvoice/CN=$domain"

# 自签名服务器证书
openssl x509 -req -passin pass:$mypass -days 36500 -in server.csr -signkey server.key -out server.crt -extensions v3_req -extfile openssl_req_config.conf

# 移除服务器密钥的密码
openssl rsa -passin pass:$mypass -in server.key -out server.key.nopass
mv server.key.nopass server.key

# 清理文件
rm server.csr openssl_req_config.conf
