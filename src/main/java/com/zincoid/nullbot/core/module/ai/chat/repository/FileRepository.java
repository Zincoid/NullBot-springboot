package com.zincoid.nullbot.core.module.ai.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zincoid.nullbot.core.model.data.dto.MessageDTO;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.repository", havingValue = "file")
public class FileRepository implements Repository {

    private static final String DIR_NAME = "chats";
    private static final String SUFFIX = ".txt";

    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Path getDir() {
        return Path.of(storageProperties.resolve(storageProperties.getTempPath())).resolve(DIR_NAME);
    }

    private Path getFile(String chatId) {
        String name = chatId.replaceAll("[\\\\/:*?\"<>|]", "_");
        return getDir().resolve(name + SUFFIX);
    }

    @Override
    public List<Message> get(String chatId) {
        Path file = getFile(chatId);
        List<Message> messages = new ArrayList<>();
        if (!Files.exists(file)) return messages;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                try {
                    messages.add(objectMapper.readValue(line, MessageDTO.class).toMessage());
                } catch (JsonProcessingException e) {
                    log.warn("▽ [FileRepository] 反序列化失败 - chatId: {}", chatId);
                }
            }
        } catch (IOException e) {
            log.warn("▽ [FileRepository] 读取失败 - chatId: {}", chatId, e);
        }
        return messages;
    }

    @Override
    public void update(String chatId, List<Message> messages) {
        try {
            StringBuilder payload = new StringBuilder();
            for (Message message : messages)
                payload.append(objectMapper.writeValueAsString(MessageDTO.of(message))).append('\n');
            Files.createDirectories(getDir());
            Path file = getFile(chatId);
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, payload.toString(), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (JsonProcessingException e) {
            log.error("▽ [FileRepository] 序列化失败 - chatId: {}", chatId, e);
        } catch (IOException e) {
            log.error("▽ [FileRepository] 写入失败 - chatId: {}", chatId, e);
        }
    }

    @Override
    public void clear(String chatId) {
        try {
            Files.deleteIfExists(getFile(chatId));
        } catch (IOException e) {
            log.warn("▽ [FileRepository] 历史清除失败 - chatId: {}", chatId, e);
        }
    }

    @Override
    public void reset() {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(getDir(), "*" + SUFFIX)) {
            for (Path file : files) Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("▽ [FileRepository] 存储重置失败", e);
        }
    }
}
