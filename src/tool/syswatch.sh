#!/bin/bash

LOGDIR="/root/Nullbot"
mkdir -p $LOGDIR

LOGFILE="$LOGDIR/sys_$(date +%Y-%m-%d_%H-%M-%S).log"

echo "===== SysWatch Started at $(date) =====" >> $LOGFILE

while true; do
    TS=$(date "+%Y-%m-%d %H:%M:%S")
    echo "" >> $LOGFILE
    echo "===== $TS =====" >> $LOGFILE

    # 1. top 一次快照
    echo "--- top -b -n 1 ---" >> $LOGFILE
    top -b -n 1 >> $LOGFILE

    # 2. 内存
    echo "--- free -m ---" >> $LOGFILE
    free -m >> $LOGFILE

    # 3. 磁盘 I/O
    echo "--- iostat -x 1 3 ---" >> $LOGFILE
    iostat -x 1 3 >> $LOGFILE

    # 4. CPU、上下文切换、内存分页
    echo "--- vmstat 1 5 ---" >> $LOGFILE
    vmstat 1 5 >> $LOGFILE

    # 5. 查看 dmesg（内核警告、OOM、硬件错误）
    echo "--- dmesg | tail -n 25 ---" >> $LOGFILE
    dmesg | tail -n 25 >> $LOGFILE

    # 6. 查看最耗资源的进程
    echo "--- ps aux --sort=-rss | head -n 10 ---" >> $LOGFILE
    ps aux --sort=-rss | head -n 10 >> $LOGFILE

    echo "--- ps aux --sort=-%cpu | head -n 10 ---" >> $LOGFILE
    ps aux --sort=-%cpu | head -n 10 >> $LOGFILE

    # 7. 网络连接
    echo "--- ss -s ---" >> $LOGFILE
    ss -s >> $LOGFILE

    sleep 5
done
