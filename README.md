# NullBot 后端文档

## 常用命令

### 快捷运行

screen -dmS napcat bash -c "xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox -q 3408240018"
screen -dmS nullbot bash -c "java -jar /root/Nullbot/target/NullBot-springboot-0.0.1-SNAPSHOT.jar"

### 使用 syswatch.sh 监控系统崩溃

chmod +x /root/Nullbot/syswatch.sh  
nohup /root/Nullbot/syswatch.sh >/dev/null 2>&1 &

### 切换 Java 版本

sudo alternatives --config java

## 本地测试部署 Chrome 版本

Google Chrome 144.0.7559.59
