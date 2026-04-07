package org.bot.nullbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NullBotApiTests
{
    private static final String PTRC_URL = "https://pointercrate.com/api/v1/players/";

    @Test
    public void testGetPlayer() throws Exception {
        int playerId = 3;  // 已知存在的玩家ID

        // 1. 第一次请求：无条件头，获取玩家信息和 ETag
        System.out.println("=== 第一次请求（无条件头）===");
        ResponseWrapper firstResponse = getPlayer(playerId, null);
        assertEquals(200, firstResponse.statusCode);
        assertNotNull(firstResponse.etag);
        System.out.println("ETag: " + firstResponse.etag);
        System.out.println("玩家数据: " + firstResponse.body);

        // 2. 第二次请求：使用 If-None-Match 带上正确的 ETag，期望 304
        System.out.println("\n=== 第二次请求（If-None-Match = 正确ETag）===");
        ResponseWrapper secondResponse = getPlayer(playerId, firstResponse.etag);
        assertEquals(304, secondResponse.statusCode);
        assertNull(secondResponse.body);  // 304 不应有响应体
        System.out.println("收到 304，内容未修改");

        // 3. 第三次请求：使用错误的 ETag，期望 200
        System.out.println("\n=== 第三次请求（If-None-Match = 错误ETag）===");
        ResponseWrapper thirdResponse = getPlayer(playerId, "\"wrong-etag-value\"");
        assertEquals(200, thirdResponse.statusCode);
        assertNotNull(thirdResponse.body);
        System.out.println("ETag 不匹配，返回完整数据");

        // 4. 测试不存在的玩家：期望 404
        System.out.println("\n=== 请求不存在的玩家（ID=99999）===");
        ResponseWrapper notFoundResponse = getPlayer(99999, null);
        assertEquals(404, notFoundResponse.statusCode);
        assertTrue(notFoundResponse.body.contains("40401"));
        System.out.println("404 错误响应: " + notFoundResponse.body);
    }

    /**
     * 发送 GET 请求获取玩家信息
     * @param playerId 玩家ID
     * @param ifNoneMatch If-None-Match 头的值（可为 null）
     * @return 包含状态码、ETag、响应体的包装对象
     */
    private ResponseWrapper getPlayer(int playerId, String ifNoneMatch) throws Exception {
        URL url = new URL(PTRC_URL + playerId + "/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (ifNoneMatch != null && !ifNoneMatch.isEmpty()) {
            conn.setRequestProperty("If-None-Match", ifNoneMatch);
        }

        int statusCode = conn.getResponseCode();
        String etag = conn.getHeaderField("ETag");

        String body = null;
        if (statusCode == 200) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }
        } else if (statusCode == 304) {
            // 304 没有响应体，但 ETag 仍然存在
            body = null;
        } else {
            // 4xx/5xx 读取错误流
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }
        }
        conn.disconnect();
        return new ResponseWrapper(statusCode, etag, body);
    }

    // 简单的响应包装类
    static class ResponseWrapper {
        int statusCode;
        String etag;
        String body;

        ResponseWrapper(int statusCode, String etag, String body) {
            this.statusCode = statusCode;
            this.etag = etag;
            this.body = body;
        }
    }
}
