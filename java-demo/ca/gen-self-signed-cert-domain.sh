set -x

mypass="sslpie"
# If you have multiple IP addresses, configure their information consecutively in the openssl_req_config.conf file.
# If there is no domain name available, you can use any arbitrary domain name and configure the corresponding relationship in the hosts file  
targetIP1="192.168.2.2"
targetIP2="192.168.3.3"
domain="www.example-asr.com"

# Generate server key
openssl genrsa -passout pass:$mypass -des3 -out server.key 4096

echo """
[req]
distinguished_name=req
[san]
subjectAltName=@alt_names
[alt_names]
IP.1 = $targetIP1
IP.2 = $targetIP2
""" > openssl_req_config.conf

# Generate server signing request:
openssl req -passin pass:$mypass -new -key server.key -out server.csr -subj  "/C=US/ST=CA/L=Beijing/O=TestVoice/OU=testvoice/CN=$domain" -extensions san -config openssl_req_config.conf

# Self-sign server certificate:
openssl x509 -req -passin pass:$mypass -extfile openssl_req_config.conf -days 36500 -in server.csr -signkey server.key -set_serial 01 -out server.crt

# Remove passphrase from server key:
openssl rsa -passin pass:$mypass -in server.key -out server.key

rm server.csr
rm openssl_req_config.conf
