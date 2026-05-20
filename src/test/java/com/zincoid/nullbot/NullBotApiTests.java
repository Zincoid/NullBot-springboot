package com.zincoid.nullbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NullBotApiTests {

    private static final String BASE_URL = "https://pointercrate.com/api/v1/players/";

    @Test
    public void testGetPlayersList() throws Exception {
        // 获取 Bearer Token
        String token = "";

        // 构造查询参数（分页 + 过滤）
        StringBuilder query = new StringBuilder();
        query.append("?limit=5");                     // 每页数量
        query.append("&offset=0");                    // 偏移量（传统分页）
        // 根据文档，也可使用基于 id 的游标分页，例如 &after=100，此处以 offset 为例
        query.append("&name=").append(URLEncoder.encode("Zincoid", StandardCharsets.UTF_8));
        // 其他可选过滤参数（根据需要取消注释）
        // query.append("&nation=US");
        // query.append("&banned=false");

        URL url = new URL(BASE_URL + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);

        int statusCode = conn.getResponseCode();
        String responseBody = readResponse(conn, statusCode);
        conn.disconnect();

        // 断言请求成功
        assertEquals(200, statusCode, "HTTP 200 期望成功获取玩家列表");
        assertNotNull(responseBody);
        assertFalse(responseBody.isEmpty());

        // 简单验证响应格式（可能是 JSON 数组或包含 data 字段的对象）
        boolean isValidJson = responseBody.trim().startsWith("[") || responseBody.contains("\"data\"");
        assertTrue(isValidJson, "响应应为 JSON 数组或对象");

        System.out.println("玩家列表响应（前200字符）: " + responseBody.substring(0, Math.min(200, responseBody.length())));
    }

    /**
     * 根据状态码读取响应体（正常流或错误流）
     */
    private String readResponse(HttpURLConnection conn, int statusCode) throws Exception {
        var stream = (statusCode >= 200 && statusCode < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
