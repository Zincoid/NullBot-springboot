package com.zincoid.nullbot.core.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
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
public final class CsvUtil {

    private static final List<String> DATE_FORMATS = Arrays.asList(
            "yyyy-MM-dd", "yyyy/MM/dd", "dd-MM-yyyy", "dd/MM/yyyy", "yyyy.MM.dd");
    private static final List<String> DATETIME_FORMATS = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS", "yyyy/MM/dd HH:mm:ss.SSS");

    private CsvUtil() {
    }

    // ==================== Import ====================

    /**
     * Import CSV from multipart file, using the first row as header.
     */
    public static <T> List<T> importCsv(MultipartFile file, Class<T> clazz) throws IOException {
        return importCsv(file, clazz, true, null);
    }

    /**
     * Import CSV from multipart file with header control and column mapping.
     */
    public static <T> List<T> importCsv(MultipartFile file, Class<T> clazz,
                                        boolean hasHeader,
                                        Map<String, String> columnMapping) throws IOException {
        log.info("▽ [CsvUtil-{}] Importing: {}, {} bytes",
                clazz.getSimpleName(), file.getOriginalFilename(), file.getSize());

        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            CSVParser parser = createParser(reader, hasHeader);
            List<String> headers = hasHeader ? new ArrayList<>(parser.getHeaderNames()) : null;

            if (hasHeader && columnMapping != null) {
                validateHeaders(headers, columnMapping, clazz);
            }

            Map<Integer, FieldMapping> fieldMapping = createFieldMapping(parser, headers, columnMapping, clazz);
            int rowNum = hasHeader ? 1 : 0;
            List<ImportError> errors = new ArrayList<>();
            List<T> result = new ArrayList<>();

            for (CSVRecord record : parser) {
                rowNum++;
                try {
                    T entity = recordToEntity(record, clazz, fieldMapping, rowNum);
                    result.add(entity);
                } catch (ImportException e) {
                    errors.add(new ImportError(rowNum, e.getMessage(), record.toString()));
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum, "System error: " + e.getMessage(), record.toString()));
                    log.warn("▽ [CsvUtil-{}] Row {} system error", clazz.getSimpleName(), rowNum, e);
                }
            }

            logImportResult(clazz, result.size(), errors, parser.getRecordNumber());

            if (!errors.isEmpty()) {
                throw new ImportException("Import had " + errors.size() + " errors", errors);
            }

            return result;
        }
    }

    /**
     * Import CSV from a file path, using the first row as header. Errors are logged but not thrown.
     */
    public static <T> List<T> importCsv(String filePath, Class<T> clazz) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {

            CSVParser parser = createParser(reader, true);
            List<String> headers = new ArrayList<>(parser.getHeaderNames());
            Map<Integer, FieldMapping> fieldMapping = createFieldMapping(parser, headers, null, clazz);
            List<T> result = new ArrayList<>();
            int rowNum = 1;

            for (CSVRecord record : parser) {
                rowNum++;
                try {
                    T entity = recordToEntity(record, clazz, fieldMapping, rowNum);
                    result.add(entity);
                } catch (ImportException e) {
                    log.warn("▽ [CsvUtil-{}] Row {} failed: {}", clazz.getSimpleName(), rowNum, e.getMessage());
                }
            }

            return result;
        }
    }

    /**
     * Import with custom converters and validator. Returns an ImportResult instead of throwing.
     */
    public static <T> ImportResult<T> importWithValidation(
            MultipartFile file, Class<T> clazz, boolean hasHeader,
            Map<String, String> columnMapping,
            Map<String, Function<String, ?>> customConverters,
            Function<T, String> validator) throws IOException {

        List<T> success = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            CSVParser parser = createParser(reader, hasHeader);
            List<String> headers = hasHeader ? new ArrayList<>(parser.getHeaderNames()) : null;
            Map<Integer, FieldMapping> fieldMapping = createFieldMapping(parser, headers, columnMapping, clazz);
            int rowNum = hasHeader ? 1 : 0;

            for (CSVRecord record : parser) {
                rowNum++;
                try {
                    T entity = recordToEntity(record, clazz, fieldMapping, rowNum);

                    if (customConverters != null) {
                        applyConverters(entity, record, fieldMapping, customConverters);
                    }

                    if (validator != null) {
                        String err = validator.apply(entity);
                        if (err != null) {
                            throw new ImportException(err, rowNum);
                        }
                    }

                    success.add(entity);
                } catch (ImportException e) {
                    errors.add(new ImportError(rowNum, e.getMessage(), record.toString()));
                } catch (Exception e) {
                    errors.add(new ImportError(rowNum, "System error: " + e.getMessage(), record.toString()));
                }
            }

            return new ImportResult<>(success, errors, (int) parser.getRecordNumber());
        }
    }

    // ==================== Export ====================

    /**
     * Export data list to CSV via HttpServletResponse.
     */
    public static <T> void exportCsv(HttpServletResponse response,
                                     String fileName,
                                     List<T> dataList,
                                     Class<T> clazz) throws IOException {
        setResponseHeaders(response, fileName);
        String[] headers = getHeaders(clazz);
        String[][] rows = toRows(dataList, clazz);
        writeCsvResponse(response, headers, rows);
    }

    /**
     * Export large datasets to CSV with paginated data supplier.
     */
    public static <T> void exportCsvLarge(HttpServletResponse response,
                                          String fileName,
                                          int pageSize,
                                          PageDataSupplier<T> dataSupplier,
                                          Class<T> clazz) {
        setResponseHeaders(response, fileName);
        String[] headers = getHeaders(clazz);

        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    response.getOutputStream(), StandardCharsets.UTF_8);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));

            int page = 1;
            List<T> batch;
            do {
                batch = dataSupplier.getData(page, pageSize);
                if (batch == null || batch.isEmpty()) {
                    break;
                }
                for (T data : batch) {
                    printer.printRecord((Object[]) toRow(data, clazz));
                }
                printer.flush();
                page++;
            } while (batch.size() == pageSize);

            printer.close();
        } catch (Exception e) {
            log.error("[CsvUtil] Large export failed", e);
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }

    // ==================== Private helpers ====================

    private static CSVParser createParser(Reader reader, boolean hasHeader) throws IOException {
        return hasHeader
                ? CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)
                : CSVFormat.DEFAULT.parse(reader);
    }

    private static void validateHeaders(List<String> headers, Map<String, String> mapping, Class<?> clazz) {
        if (headers == null || headers.isEmpty()) {
            throw new ImportException("CSV file has no header");
        }
        for (Map.Entry<String, String> e : mapping.entrySet()) {
            if (!headers.contains(e.getKey())) {
                throw new ImportException("CSV missing column: " + e.getKey());
            }
            try {
                clazz.getDeclaredField(e.getValue());
            } catch (NoSuchFieldException ex) {
                throw new ImportException("Entity missing field: " + e.getValue());
            }
        }
    }

    private static <T> Map<Integer, FieldMapping> createFieldMapping(
            CSVParser parser, List<String> headers,
            Map<String, String> columnMapping, Class<T> clazz) {

        Map<Integer, FieldMapping> mapping = new LinkedHashMap<>();

        if (headers != null && !headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                String csvCol = headers.get(i);
                String fieldName = columnMapping != null && columnMapping.containsKey(csvCol)
                        ? columnMapping.get(csvCol) : csvCol;
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    mapping.put(i, new FieldMapping(field, fieldName));
                } catch (NoSuchFieldException e) {
                    log.debug("▽ [CsvUtil-{}] Column '{}' has no matching field '{}'",
                            clazz.getSimpleName(), csvCol, fieldName);
                }
            }
        } else {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < Math.min(fields.length, parser.getHeaderNames().size()); i++) {
                fields[i].setAccessible(true);
                mapping.put(i, new FieldMapping(fields[i], fields[i].getName()));
            }
        }

        return mapping;
    }

    private static <T> T recordToEntity(CSVRecord record, Class<T> clazz,
                                        Map<Integer, FieldMapping> fieldMapping,
                                        int rowNum) throws ImportException {
        try {
            Constructor<T> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            T entity = ctor.newInstance();

            for (Map.Entry<Integer, FieldMapping> e : fieldMapping.entrySet()) {
                int col = e.getKey();
                FieldMapping fm = e.getValue();
                if (col < record.size()) {
                    String value = record.get(col);
                    if (value != null && !value.trim().isEmpty()) {
                        setFieldValue(entity, fm.field, value.trim(), rowNum, col);
                    }
                }
            }

            return entity;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new ImportException("Failed to create entity: " + e.getMessage(), rowNum);
        }
    }

    private static void setFieldValue(Object entity, Field field, String value,
                                      int rowNum, int colNum) throws ImportException {
        try {
            field.set(entity, convertValue(value, field.getType()));
        } catch (IllegalAccessException e) {
            throw new ImportException("Failed to set field: " + e.getMessage(), rowNum);
        } catch (Exception e) {
            throw new ImportException(
                    String.format("Row %d col %d: cannot convert '%s' to %s — %s",
                            rowNum, colNum + 1, value, field.getType().getSimpleName(), e.getMessage()),
                    rowNum);
        }
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) return null;
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value);
        if (targetType == Float.class || targetType == float.class) return Float.parseFloat(value);
        if (targetType == Boolean.class || targetType == boolean.class) return parseBoolean(value);
        if (targetType == Date.class) return parseDate(value);
        if (targetType == LocalDate.class) return parseLocalDate(value);
        if (targetType == LocalDateTime.class) return parseLocalDateTime(value);
        if (targetType.isEnum()) return parseEnum(value, (Class<Enum>) targetType);

        try {
            return targetType.getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported type: " + targetType.getName());
        }
    }

    private static Boolean parseBoolean(String value) {
        String v = value.toLowerCase();
        if ("true".equals(v) || "是".equals(v) || "1".equals(v) || "yes".equals(v) || "y".equals(v)) return true;
        if ("false".equals(v) || "否".equals(v) || "0".equals(v) || "no".equals(v) || "n".equals(v)) return false;
        throw new IllegalArgumentException("Cannot parse boolean: " + value);
    }

    private static Date parseDate(String value) {
        for (String fmt : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(fmt).parse(value);
            } catch (ParseException ignored) {
            }
        }
        for (String fmt : DATETIME_FORMATS) {
            try {
                return new SimpleDateFormat(fmt).parse(value);
            } catch (ParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Cannot parse date: " + value);
    }

    private static LocalDate parseLocalDate(String value) {
        for (String fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Cannot parse LocalDate: " + value);
    }

    private static LocalDateTime parseLocalDateTime(String value) {
        for (String fmt : DATETIME_FORMATS) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Cannot parse LocalDateTime: " + value);
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> enumType) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            for (E constant : enumType.getEnumConstants()) {
                if (constant.toString().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value: " + value);
        }
    }

    // ==================== Export helpers ====================

    private static void setResponseHeaders(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encoded + ".csv\"");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private static String[] getHeaders(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toArray(String[]::new);
    }

    private static <T> String[][] toRows(List<T> dataList, Class<T> clazz) {
        return dataList.stream()
                .map(data -> toRow(data, clazz))
                .toArray(String[][]::new);
    }

    private static <T> String[] toRow(T data, Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String[] row = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                Object value = fields[i].get(data);
                row[i] = value != null ? value.toString() : "";
            } catch (IllegalAccessException e) {
                row[i] = "";
            }
        }
        return row;
    }

    private static void writeCsvResponse(HttpServletResponse response,
                                         String[] headers, String[][] rows) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(
                response.getOutputStream(), StandardCharsets.UTF_8);
        writer.write('﻿'); // BOM for Excel UTF-8 recognition

        try (CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {
            for (String[] row : rows) {
                printer.printRecord((Object[]) row);
            }
            printer.flush();
        }
    }

    private static <T> void applyConverters(T entity, CSVRecord record,
                                            Map<Integer, FieldMapping> fieldMapping,
                                            Map<String, Function<String, ?>> converters)
            throws IllegalAccessException {
        for (Map.Entry<Integer, FieldMapping> e : fieldMapping.entrySet()) {
            FieldMapping fm = e.getValue();
            if (converters.containsKey(fm.fieldName) && e.getKey() < record.size()) {
                String value = record.get(e.getKey());
                if (value != null && !value.trim().isEmpty()) {
                    fm.field.set(entity, converters.get(fm.fieldName).apply(value.trim()));
                }
            }
        }
    }

    private static void logImportResult(Class<?> clazz, int success, List<ImportError> errors, long total) {
        if (errors.isEmpty()) {
            log.info("▽ [CsvUtil-{}] All {} rows imported", clazz.getSimpleName(), total);
        } else {
            log.warn("▽ [CsvUtil-{}] {} total, {} success, {} failed",
                    clazz.getSimpleName(), total, success, errors.size());
            errors.stream().limit(10).forEach(e ->
                    log.warn("▽ [CsvUtil-{}] Row {}: {}", clazz.getSimpleName(), e.rowNum(), e.message()));
            if (errors.size() > 10) {
                log.warn("▽ [CsvUtil-{}] ... {} more errors", clazz.getSimpleName(), errors.size() - 10);
            }
        }
    }

    // ==================== Inner types ====================

    private record FieldMapping(Field field, String fieldName) {
    }

    public record ImportError(int rowNum, String message, String rawData) {
    }

    @Getter
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

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }

    @Getter
    public static class ImportResult<T> {
        private final List<T> successData;
        private final List<ImportError> errors;
        private final int totalRows;

        public ImportResult(List<T> successData, List<ImportError> errors, int totalRows) {
            this.successData = successData != null ? successData : Collections.emptyList();
            this.errors = errors != null ? errors : Collections.emptyList();
            this.totalRows = totalRows;
        }

        public int getSuccessCount() {
            return successData.size();
        }

        public int getErrorCount() {
            return errors.size();
        }

        public boolean isAllSuccess() {
            return errors.isEmpty();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public double getSuccessRate() {
            return totalRows > 0 ? (successData.size() * 100.0 / totalRows) : 0.0;
        }
    }

    @FunctionalInterface
    public interface PageDataSupplier<T> {
        List<T> getData(int page, int pageSize);
    }
}
