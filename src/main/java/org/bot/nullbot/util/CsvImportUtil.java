package org.bot.nullbot.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

@Slf4j
public class CsvImportUtil
{
    // 默认的日期时间格式
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 常用日期格式列表，用于尝试解析
    private static final List<String> DATE_FORMATS = Arrays.asList(
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy.MM.dd"
    );

    private static final List<String> DATETIME_FORMATS = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy/MM/dd HH:mm:ss.SSS"
    );

    /**
     * 从CSV文件导入数据
     *
     * @param file     上传的CSV文件
     * @param clazz    目标实体类类型
     * @param <T>      泛型
     * @return         导入的数据列表
     */
    public static <T> List<T> importFromCsv(MultipartFile file, Class<T> clazz) throws IOException {
        return importFromCsv(file, clazz, true, null);
    }

    /**
     * 从CSV文件导入数据（带自定义映射）
     *
     * @param file               上传的CSV文件
     * @param clazz              目标实体类类型
     * @param hasHeader          是否包含表头
     * @param columnMapping      列映射（CSV列名 -> 实体字段名），如果为null则使用CSV表头
     * @param <T>                泛型
     * @return                   导入的数据列表
     */
    public static <T> List<T> importFromCsv(MultipartFile file,
                                            Class<T> clazz,
                                            boolean hasHeader,
                                            Map<String, String> columnMapping) throws IOException {
        log.info("[ImportCsv-{}] 开始导入CSV文件: {}, 大小: {} bytes",
                clazz.getSimpleName(), file.getOriginalFilename(), file.getSize());

        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            // 解析CSV文件
            CSVParser csvParser;
            if (hasHeader) {
                csvParser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(reader);
            } else {
                csvParser = CSVFormat.DEFAULT.parse(reader);
            }

            List<T> resultList = new ArrayList<>();
            List<String> headers = hasHeader ?
                    new ArrayList<>(csvParser.getHeaderNames()) : null;

            // 验证表头
            if (hasHeader && columnMapping != null) {
                validateHeaders(headers, columnMapping, clazz);
            }

            // 字段映射关系
            Map<Integer, FieldMapping> fieldMapping = createFieldMapping(
                    csvParser, headers, columnMapping, clazz);

            // 处理每一行数据
            int rowNum = hasHeader ? 1 : 0;
            List<ImportError> errors = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                rowNum++;
                try {
                    T entity = convertRecordToEntity(record, clazz, fieldMapping, rowNum);
                    if (entity != null) {
                        resultList.add(entity);
                        log.debug("[ImportCsv-{}] 成功导入第{}行数据",
                                clazz.getSimpleName(), rowNum);
                    }
                } catch (ImportException e) {
                    errors.add(new ImportError(rowNum, e.getMessage(), record.toString()));
                    log.warn("[ImportCsv-{}] 第{}行数据导入失败: {}",
                            clazz.getSimpleName(), rowNum, e.getMessage());
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum, "系统错误: " + e.getMessage(), record.toString()));
                    log.error("[ImportCsv-{}] 第{}行数据导入时发生系统错误",
                            clazz.getSimpleName(), rowNum, e);
                }
            }

            // 记录导入结果
            logImportResult(clazz, resultList.size(), errors, csvParser.getRecordNumber());

            // 如果有错误，抛出包含错误信息的异常
            if (!errors.isEmpty()) {
                throw new ImportException("导入过程中发现" + errors.size() + "个错误", errors);
            }

            return resultList;
        }
    }

    /**
     * 从文件路径导入CSV数据
     *
     * @param filePath 文件路径
     * @param clazz    目标实体类类型
     * @param <T>      泛型
     * @return         导入的数据列表
     */
    public static <T> List<T> importFromCsv(String filePath, Class<T> clazz) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {

            return importFromCsv(reader, clazz, true, null);
        }
    }

    /**
     * 从Reader导入CSV数据
     */
    private static <T> List<T> importFromCsv(Reader reader,
                                             Class<T> clazz,
                                             boolean hasHeader,
                                             Map<String, String> columnMapping) throws IOException {
        CSVParser csvParser;
        if (hasHeader) {
            csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
        } else {
            csvParser = CSVFormat.DEFAULT.parse(reader);
        }

        List<T> resultList = new ArrayList<>();
        List<String> headers = hasHeader ?
                new ArrayList<>(csvParser.getHeaderNames()) : null;

        Map<Integer, FieldMapping> fieldMapping = createFieldMapping(
                csvParser, headers, columnMapping, clazz);

        int rowNum = hasHeader ? 1 : 0;

        for (CSVRecord record : csvParser) {
            rowNum++;
            try {
                T entity = convertRecordToEntity(record, clazz, fieldMapping, rowNum);
                if (entity != null) {
                    resultList.add(entity);
                }
            } catch (ImportException e) {
                log.warn("第{}行数据导入失败: {}", rowNum, e.getMessage());
                // 可以根据需要决定是否继续处理
            }
        }

        return resultList;
    }

    /**
     * 验证表头
     */
    private static void validateHeaders(List<String> headers,
                                        Map<String, String> columnMapping,
                                        Class<?> clazz) {
        if (headers == null || headers.isEmpty()) {
            throw new ImportException("CSV文件缺少表头");
        }

        // 检查必需的映射字段是否存在
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            String csvColumn = entry.getKey();
            if (!headers.contains(csvColumn)) {
                throw new ImportException("CSV文件缺少必需的列: " + csvColumn);
            }

            // 检查实体类是否有对应的字段
            try {
                Field field = clazz.getDeclaredField(entry.getValue());
                if (field == null) {
                    throw new ImportException("实体类缺少字段: " + entry.getValue());
                }
            } catch (NoSuchFieldException e) {
                throw new ImportException("实体类缺少字段: " + entry.getValue());
            }
        }
    }

    /**
     * 创建字段映射关系
     */
    private static <T> Map<Integer, FieldMapping> createFieldMapping(
            CSVParser csvParser,
            List<String> headers,
            Map<String, String> columnMapping,
            Class<T> clazz) {

        Map<Integer, FieldMapping> mapping = new HashMap<>();

        // 如果有表头，使用表头映射
        if (headers != null && !headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                String csvColumn = headers.get(i);
                String fieldName = columnMapping != null && columnMapping.containsKey(csvColumn)
                        ? columnMapping.get(csvColumn)
                        : csvColumn;

                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    mapping.put(i, new FieldMapping(field, fieldName));
                } catch (NoSuchFieldException e) {
                    // 如果实体类没有这个字段，记录警告但不中断
                    log.warn("[ImportCsv-{}] CSV列'{}'在实体类中没有对应的字段'{}'",
                            clazz.getSimpleName(), csvColumn, fieldName);
                }
            }
        }
        // 如果没有表头，按顺序映射到实体类的字段
        else {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < Math.min(fields.length, csvParser.getHeaderNames().size()); i++) {
                fields[i].setAccessible(true);
                mapping.put(i, new FieldMapping(fields[i], fields[i].getName()));
            }
        }

        return mapping;
    }

    /**
     * 将CSV记录转换为实体对象
     */
    private static <T> T convertRecordToEntity(CSVRecord record,
                                               Class<T> clazz,
                                               Map<Integer, FieldMapping> fieldMapping,
                                               int rowNum) throws ImportException {
        try {
            // 创建实体实例
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();

            // 设置字段值
            for (Map.Entry<Integer, FieldMapping> entry : fieldMapping.entrySet()) {
                int columnIndex = entry.getKey();
                FieldMapping mapping = entry.getValue();

                if (columnIndex < record.size()) {
                    String value = record.get(columnIndex);
                    if (value != null && !value.trim().isEmpty()) {
                        setFieldValue(entity, mapping.getField(), value, rowNum, columnIndex);
                    }
                }
            }

            // 验证实体（可选）
            validateEntity(entity, rowNum);

            return entity;

        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new ImportException("创建实体对象失败: " + e.getMessage(), rowNum);
        }
    }

    /**
     * 设置字段值
     */
    private static void setFieldValue(Object entity,
                                      Field field,
                                      String value,
                                      int rowNum,
                                      int colNum) throws ImportException {
        try {
            Class<?> fieldType = field.getType();
            Object convertedValue = convertStringToType(value.trim(), fieldType);
            field.set(entity, convertedValue);

        } catch (IllegalAccessException e) {
            throw new ImportException("设置字段值失败: " + e.getMessage(), rowNum);
        } catch (Exception e) {
            throw new ImportException(
                    String.format("第%d行第%d列数据转换失败(%s -> %s): %s",
                            rowNum, colNum + 1, value, field.getType().getSimpleName(), e.getMessage()),
                    rowNum);
        }
    }

    /**
     * 字符串到目标类型的转换
     */
    private static Object convertStringToType(String value, Class<?> targetType) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            if (targetType.equals(String.class)) {
                return value;
            } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(value);
            } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(value);
            } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return Float.parseFloat(value);
            } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return parseBoolean(value);
            } else if (targetType.equals(Date.class)) {
                return parseDate(value);
            } else if (targetType.equals(LocalDate.class)) {
                return parseLocalDate(value);
            } else if (targetType.equals(LocalDateTime.class)) {
                return parseLocalDateTime(value);
            } else if (targetType.isEnum()) {
                return parseEnum(value, (Class<Enum>) targetType);
            } else {
                // 尝试使用字符串构造函数
                try {
                    return targetType.getConstructor(String.class).newInstance(value);
                } catch (Exception e) {
                    throw new IllegalArgumentException("不支持的类型转换: " + targetType.getName());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将值'" + value + "'转换为类型" + targetType.getName() + ": " + e.getMessage());
        }
    }

    /**
     * 解析布尔值
     */
    private static Boolean parseBoolean(String value) {
        String lowerValue = value.toLowerCase();
        if (lowerValue.equals("true") || lowerValue.equals("是") ||
                lowerValue.equals("1") || lowerValue.equals("yes") ||
                lowerValue.equals("y")) {
            return true;
        } else if (lowerValue.equals("false") || lowerValue.equals("否") ||
                lowerValue.equals("0") || lowerValue.equals("no") ||
                lowerValue.equals("n")) {
            return false;
        }
        throw new IllegalArgumentException("无法解析为布尔值: " + value);
    }

    /**
     * 解析日期
     */
    private static Date parseDate(String value) {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(value);
            } catch (ParseException e) {
                // 继续尝试下一个格式
            }
        }

        // 尝试日期时间格式
        for (String format : DATETIME_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(value);
            } catch (ParseException e) {
                // 继续尝试下一个格式
            }
        }

        throw new IllegalArgumentException("无法解析为日期: " + value);
    }

    /**
     * 解析LocalDate
     */
    private static LocalDate parseLocalDate(String value) {
        for (String format : DATE_FORMATS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("无法解析为LocalDate: " + value);
    }

    /**
     * 解析LocalDateTime
     */
    private static LocalDateTime parseLocalDateTime(String value) {
        for (String format : DATETIME_FORMATS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("无法解析为LocalDateTime: " + value);
    }

    /**
     * 解析枚举值
     */
    private static <E extends Enum<E>> E parseEnum(String value, Class<E> enumType) {
        try {
            // 尝试按名称匹配
            return Enum.valueOf(enumType, value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // 尝试按toString匹配
            for (E enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.toString().equalsIgnoreCase(value)) {
                    return enumConstant;
                }
            }
            throw new IllegalArgumentException("无效的枚举值: " + value);
        }
    }

    /**
     * 验证实体对象（子类可覆盖）
     */
    private static void validateEntity(Object entity, int rowNum) throws ImportException {
        // 默认实现不做验证，子类可覆盖
    }

    /**
     * 记录导入结果
     */
    private static void logImportResult(Class<?> clazz, int successCount,
                                        List<ImportError> errors, long totalRows) {
        if (errors.isEmpty()) {
            log.info("[ImportCsv-{}] 导入成功: 总共{}行，全部导入成功",
                    clazz.getSimpleName(), totalRows);
        } else {
            log.warn("[ImportCsv-{}] 导入完成: 总共{}行，成功{}行，失败{}行",
                    clazz.getSimpleName(), totalRows, successCount, errors.size());

            // 记录前10个错误
            errors.stream().limit(10).forEach(error ->
                    log.warn("[ImportCsv-{}] 第{}行导入失败: {}",
                            clazz.getSimpleName(), error.getRowNum(), error.getMessage()));

            if (errors.size() > 10) {
                log.warn("[ImportCsv-{}] ... 还有{}个错误未显示",
                        clazz.getSimpleName(), errors.size() - 10);
            }
        }
    }

    /**
     * 导入异常类
     */
    public static class ImportException extends RuntimeException {
        private final int rowNum;
        private final List<ImportError> errors;

        public ImportException(String message) {
            this(message, -1);
        }

        public ImportException(String message, int rowNum) {
            super(message);
            this.rowNum = rowNum;
            this.errors = Collections.emptyList();
        }

        public ImportException(String message, List<ImportError> errors) {
            super(message);
            this.rowNum = -1;
            this.errors = errors != null ? errors : Collections.emptyList();
        }

        public int getRowNum() {
            return rowNum;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    /**
     * 导入错误信息
     */
    public static class ImportError {
        private final int rowNum;
        private final String message;
        private final String rawData;

        public ImportError(int rowNum, String message, String rawData) {
            this.rowNum = rowNum;
            this.message = message;
            this.rawData = rawData;
        }

        public int getRowNum() {
            return rowNum;
        }

        public String getMessage() {
            return message;
        }

        public String getRawData() {
            return rawData;
        }
    }

    /**
     * 字段映射信息
     */
    private static class FieldMapping {
        private final Field field;
        private final String fieldName;

        public FieldMapping(Field field, String fieldName) {
            this.field = field;
            this.fieldName = fieldName;
        }

        public Field getField() {
            return field;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    /**
     * 导入结果包装类
     */
    public static class ImportResult<T> {
        private final List<T> successData;
        private final List<ImportError> errors;
        private final int totalRows;
        private final int successCount;
        private final int errorCount;

        public ImportResult(List<T> successData, List<ImportError> errors, int totalRows) {
            this.successData = successData != null ? successData : Collections.emptyList();
            this.errors = errors != null ? errors : Collections.emptyList();
            this.totalRows = totalRows;
            this.successCount = this.successData.size();
            this.errorCount = this.errors.size();
        }

        public List<T> getSuccessData() {
            return successData;
        }

        public List<ImportError> getErrors() {
            return errors;
        }

        public int getTotalRows() {
            return totalRows;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public boolean isAllSuccess() {
            return errorCount == 0;
        }

        public boolean hasErrors() {
            return errorCount > 0;
        }

        public double getSuccessRate() {
            return totalRows > 0 ? (successCount * 100.0 / totalRows) : 0.0;
        }
    }

    /**
     * 高级导入方法：支持自定义转换器和验证器
     */
    public static <T> ImportResult<T> importWithValidation(
            MultipartFile file,
            Class<T> clazz,
            boolean hasHeader,
            Map<String, String> columnMapping,
            Map<String, Function<String, ?>> customConverters,
            Function<T, String> validator) throws IOException {

        List<T> successData = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            CSVParser csvParser = hasHeader ?
                    CSVFormat.DEFAULT
                            .builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .build()
                            .parse(reader) :
                    CSVFormat.DEFAULT.parse(reader);

            List<String> headers = hasHeader ?
                    new ArrayList<>(csvParser.getHeaderNames()) : null;

            Map<Integer, FieldMapping> fieldMapping = createFieldMapping(
                    csvParser, headers, columnMapping, clazz);

            int rowNum = hasHeader ? 1 : 0;

            for (CSVRecord record : csvParser) {
                rowNum++;
                try {
                    T entity = convertRecordToEntity(record, clazz, fieldMapping, rowNum);

                    // 应用自定义转换器
                    if (customConverters != null) {
                        applyCustomConverters(entity, record, fieldMapping, customConverters);
                    }

                    // 验证实体
                    if (validator != null) {
                        String validationError = validator.apply(entity);
                        if (validationError != null) {
                            throw new ImportException(validationError, rowNum);
                        }
                    }

                    successData.add(entity);

                } catch (ImportException e) {
                    errors.add(new ImportError(rowNum, e.getMessage(), record.toString()));
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum, "系统错误: " + e.getMessage(), record.toString()));
                }
            }

            return new ImportResult<>(successData, errors, (int) csvParser.getRecordNumber());
        }
    }

    /**
     * 应用自定义转换器
     */
    private static <T> void applyCustomConverters(T entity,
                                                  CSVRecord record,
                                                  Map<Integer, FieldMapping> fieldMapping,
                                                  Map<String, Function<String, ?>> customConverters)
            throws IllegalAccessException {

        for (Map.Entry<Integer, FieldMapping> entry : fieldMapping.entrySet()) {
            int columnIndex = entry.getKey();
            FieldMapping mapping = entry.getValue();
            String fieldName = mapping.getFieldName();

            if (customConverters.containsKey(fieldName) && columnIndex < record.size()) {
                String value = record.get(columnIndex);
                if (value != null && !value.trim().isEmpty()) {
                    Function<String, ?> converter = customConverters.get(fieldName);
                    Object convertedValue = converter.apply(value.trim());
                    mapping.getField().set(entity, convertedValue);
                }
            }
        }
    }
}
