# NapCat 运行命令
sudo screen -S napcat

sudo xvfb-run -a /root/Napcat/opt/QQ/qq --no-sandbox

按 Ctrl+A，然后按 D 分离会话

# NullBot 运行命令
sudo screen -S nullbot

sudo java -jar /root/Nullbot/jar/NullBot-0.0.1-SNAPSHOT.jar

按 Ctrl+A，然后按 D 分离会话

# 查看所有会话
sudo screen -ls

# 恢复会话
sudo screen -r napcat  # 恢复 napcat

sudo screen -r nullbot # 恢复 nullbot

# 完全结束会话
在会话内输入 exit

# 本地访问服务器NapCat配置服务
ssh -L 6099:127.0.0.1:6099 root@101.200.136.96