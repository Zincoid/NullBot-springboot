package com.zincoid.nullbot.util;

import com.zincoid.nullbot.entity.info.DuelInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class DuelUtil {

    private DuelUtil() {}

    /**
     * CSV文件中随机读取一行加载为 DuelInfo 对象
     * @param filePath CSV 文件路径
     * @return DuelInfo 对象
     */
    public static DuelInfo getRandom(String filePath) {
        Path path = Paths.get(filePath);
        long dataLineCount;
        // 计算数据行数 (排除表头)
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(filePath))) {
            lnr.skip(Long.MAX_VALUE);
            dataLineCount = lnr.getLineNumber() - 1;
            if (dataLineCount <= 0)
                throw new RuntimeException("文件没有数据行");
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
        // 随机选择数据 (跳过表头)
        Random random = new Random();
        long targetLine = random.nextLong(dataLineCount) + 1;
        // 读取指定数据
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            long currentLine;
            reader.readLine();  // 跳过表头
            currentLine = 1;  // 现在 currentLine 对应实际数据行号
            while ((line = reader.readLine()) != null) {
                if (currentLine == targetLine)
                    return parseLine(line);
                currentLine++;
            }
        } catch (IOException e) {
            throw new RuntimeException("IO出错: " + e.getMessage());
        }
        throw new RuntimeException("无法读取指定行");
    }

    /**
     * 解析CSV行数据为Duel对象
     * @param line CSV行数据
     * @return Duel对象
     */
    private static DuelInfo parseLine(String line) {
        String[] values = line.split(",");
        Map<Integer, Integer> left = new HashMap<>();
        for (int i = 0; i < 56 && i < values.length - 1; i++) {
            try {  // 处理前 56 列 (索引 0-55)
                int value = Integer.parseInt(values[i].trim());
                if (value != 0) {
                    left.put(i + 1, value); // 列序号从 1 开始
                }
            } catch (NumberFormatException e) {
                // 忽略无法解析的值
            }
        }
        Map<Integer, Integer> right = new HashMap<>();
        for (int i = 56; i < 112 && i < values.length - 1; i++) {
            try {  // 处理中间 56 列 (索引 56-111)
                int value = Integer.parseInt(values[i].trim());
                if (value != 0) {
                    right.put(i - 55, value); // 列序号从 1 开始并减去 56
                }
            } catch (NumberFormatException e) {
                // 忽略无法解析的值
            }
        }
        String winner = "L";  // 获取第 113 列的值 默认 L (winner)
        if (values.length >= 113) {
            winner = values[112].trim(); // 索引 112 对应第 113 列
        }
        return new DuelInfo(left, right, winner);
    }
}
