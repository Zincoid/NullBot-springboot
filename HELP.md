# 快捷运行
screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox -q 3408240018 > /root/Napcat/qq.out.log 2>&1"  
screen -dmS nullbot bash -c "java -jar /root/Nullbot/jar/NullBot-springboot-0.0.1-SNAPSHOT.jar 2>&1 | tee /root/Nullbot/output.log"  

# 附加方法
### 限制Napcat
screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox --disable-crash-reporter --disable-logging --log-level=3 --disable-gpu --disable-features=UseOzonePlatform -q 3408240018 > /root/Napcat/qq.out.log 2>&1"  

### 使用syswatch.sh监控系统崩溃
chmod +x /root/Nullbot/syswatch.sh  
nohup /root/Nullbot/syswatch.sh >/dev/null 2>&1 &

### 使用Napcat service
systemctl daemon-reload  
systemctl enable napcat
systemctl start napcat

### 切换Java版本
sudo alternatives --config java
### 指定输出文件 (nohup)
nohup java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar > output.log 2>&1 &

# 基础命令
### NapCat 基础运行命令
sudo screen -S napcat  
sudo xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox  

### NullBot 基础运行命令
sudo screen -S nullbot  
java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar
