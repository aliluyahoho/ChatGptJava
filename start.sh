# 安装代理
#!/bin/sh
cd clash
nohup ./clash-linux-amd64-v1.10.0 -f glados.yaml -d . > /dev/null 2>&1 &
java -jar /app/springboot-wxcloudrun-1.0.jar
