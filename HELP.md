# 快捷运行
screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox  -q 3408240018"  
screen -dmS nullbot bash -c "java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar 2>&1 | tee /root/Nullbot/output.log"  

### 限制napcat
screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox --disable-crash-reporter --disable-logging --log-level=3 --disable-gpu --disable-features=UseOzonePlatform -q 3408240018 > /root/Napcat/qq.out.log 2>&1"  

screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq \
--no-sandbox \
--disable-gpu \
--disable-software-rasterizer \
--disable-features=UseOzonePlatform \
-q 3408240018"

screen -S napcat
xvfb-run -a /root/Napcat/opt/QQ/qq \
--no-sandbox \
--disable-crash-reporter \
--disable-logging \
--log-level=3 \
--disable-gpu \
--disable-software-rasterizer \
-q 3408240018 > qq.out.log 2>&1

# 启动命令
### NapCat 基础运行命令
sudo screen -S napcat  
sudo xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox  

### NullBot 基础运行命令
sudo screen -S nullbot  
java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar

### 本地访问服务器NapCat配置服务
ssh -L 6099:127.0.0.1:6099 root@ip

### 使用syswatch.sh监控系统崩溃
chmod +x /root/Nullbot/syswatch.sh  
nohup /root/Nullbot/syswatch.sh >/dev/null 2>&1 &

### Napcat service
systemctl daemon-reload  
systemctl enable napcat
systemctl start napcat

### nohup指定输出到指定文件
nohup java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar > output.log 2>&1 &

### Java版本切换
sudo alternatives --config java
