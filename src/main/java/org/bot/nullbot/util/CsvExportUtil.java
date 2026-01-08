package org.bot.nullbot.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CsvExportUtil
{
    /**
     * 导出数据到CSV文件
     * @param response HttpServletResponse
     * @param fileName 文件名（不带扩展名）
     * @param dataList 数据列表
     * @param clazz 实体类类型
     * @param <T> 泛型
     */
    public static <T> void exportToCsv(HttpServletResponse response,
                                       String fileName,
                                       List<T> dataList,
                                       Class<T> clazz) throws IOException, IllegalAccessException {
        try {
            // 设置响应头
            setResponseHeaders(response, fileName);
            log.info("[ExportCsv-{}] 已设置响应头", clazz.getSimpleName());

            // 获取CSV表头
            String[] headers = getCsvHeaders(clazz);
            log.info("[ExportCsv-{}] 已获取CSV表头: {}", clazz.getSimpleName(), Arrays.toString(headers));

            // 获取CSV数据行
            List<String[]> dataRows = getCsvDataRows(dataList, clazz);
            log.info("[ExportCsv-{}] 已获取CSV数据行", clazz.getSimpleName());

            // 写入CSV文件
            writeCsvToResponse(response, headers, dataRows);
            log.info("[ExportCsv-{}] 已写入CSV文件", clazz.getSimpleName());

        } catch (Exception e) {
            log.error("[ExportCsv-{}] 导出CSV文件失败: {}", clazz.getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 分页导出大数据量到CSV
     * @param response HttpServletResponse
     * @param fileName 文件名
     * @param pageSize 每页大小
     * @param dataSupplier 数据提供函数（接收页码和页大小，返回数据列表）
     * @param clazz 实体类类型
     * @param <T> 泛型
     */
    public static <T> void exportLargeDataToCsv(HttpServletResponse response,
                                                String fileName,
                                                int pageSize,
                                                PageDataSupplier<T> dataSupplier,
                                                Class<T> clazz) {
        try {
            // 设置响应头
            setResponseHeaders(response, fileName);

            // 获取CSV表头
            String[] headers = getCsvHeaders(clazz);

            // 创建CSV打印机
            OutputStreamWriter writer = new OutputStreamWriter(
                    response.getOutputStream(), StandardCharsets.UTF_8);
            CSVPrinter csvPrinter = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.withHeader(headers));

            // 分页查询并写入
            int page = 1;
            List<T> dataList;

            do {
                dataList = dataSupplier.getData(page, pageSize);
                if (dataList == null || dataList.isEmpty()) {
                    break;
                }

                // 写入当前页数据
                for (T data : dataList) {
                    String[] row = convertObjectToArray(data, clazz);
                    csvPrinter.printRecord((Object[]) row);
                }

                // 刷新缓冲区
                csvPrinter.flush();

                page++;
            } while (dataList.size() == pageSize);

            csvPrinter.close();

        } catch (Exception e) {
            log.error("分页导出CSV文件失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    private static void setResponseHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + ".csv\"");

        // 防止浏览器缓存
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private static <T> String[] getCsvHeaders(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<String> headers = new ArrayList<>();

        for (Field field : fields) {
            // 可以在这里添加注解过滤逻辑
            // 例如：如果字段上有@TableField注解且exist=false，则跳过
            headers.add(field.getName());
        }

        return headers.toArray(new String[0]);
    }

    private static <T> List<String[]> getCsvDataRows(List<T> dataList, Class<T> clazz)
            throws IllegalAccessException {
        List<String[]> rows = new ArrayList<>();

        for (T data : dataList) {
            String[] row = convertObjectToArray(data, clazz);
            rows.add(row);
        }

        return rows;
    }

    private static <T> String[] convertObjectToArray(T data, Class<T> clazz)
            throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        String[] row = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            Object value = fields[i].get(data);
            row[i] = value == null ? "" : value.toString();
        }

        return row;
    }

    private static void writeCsvToResponse(HttpServletResponse response,
                                           String[] headers,
                                           List<String[]> dataRows) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(
                response.getOutputStream(), StandardCharsets.UTF_8);

        // 写入BOM，确保Excel正确识别UTF-8编码
        writer.write('\ufeff');

        try (CSVPrinter csvPrinter = new CSVPrinter(writer,
                CSVFormat.DEFAULT.withHeader(headers))) {

            for (String[] row : dataRows) {
                csvPrinter.printRecord((Object[]) row);
            }

            csvPrinter.flush();
        }
    }

    @FunctionalInterface
    public interface PageDataSupplier<T> {
        List<T> getData(int page, int pageSize);
    }
}