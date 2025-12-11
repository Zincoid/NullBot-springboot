# 快捷运行
screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox  -q 3408240018"  
screen -dmS nullbot bash -c "java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar 2>&1 | tee /root/Nullbot/output.log"

# 启动命令
### NapCat 运行命令
sudo screen -S napcat  
sudo xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox  

### NullBot 运行命令
sudo screen -S nullbot  
java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar

### Screen 用法
sudo screen -ls  查看所有会话  
sudo screen -r  会话名
按 Ctrl+A，然后按 D 分离会话  
在会话内输入 exit 结束会话

### 本地访问服务器NapCat配置服务
ssh -L 6099:127.0.0.1:6099 root@ip

### 性能分析
sudo perf record -g java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar  
sudo perf report -i perf.data

### 指定输出到指定文件
nohup java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar > output.log 2>&1 &
java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar 2>&1 | tee /root/Nullbot/output.log

### Java版本切换
sudo alternatives --config java