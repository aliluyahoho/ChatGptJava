# 安装代理
apk add wget --no-cache --no-progress
apk add unzip --no-cache --no-progress
wget -O clash.zip https://glados.rocks/tools/clash-linux.zip
unzip clash.zip
wget -O ./clash/glados.yaml https://update.glados-config.com/clash/110828/093378c/82919/glados-terminal.yaml
chmod +x ./clash/clash-linux-amd64-v1.10.0
cd clash
nohup ./clash-linux-amd64-v1.10.0 -f glados.yaml -d . > /dev/null 2>&1 &
java -jar /app/springboot-wxcloudrun-1.0.jar
