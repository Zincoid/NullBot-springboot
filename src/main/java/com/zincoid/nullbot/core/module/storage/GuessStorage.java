package com.zincoid.nullbot.core.module.storage;

import com.zincoid.nullbot.core.exception.CoreException;
import lombok.Data;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.GuessData;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Data
public class GuessStorage {

    private final FileService fileService;
    private final Map<Long, GuessData> guesses;
    private final String dataPath;

    public GuessStorage(FileService fileService, StorageProperties storageProperties) {
        this.fileService = fileService;
        this.guesses = new ConcurrentHashMap<>();
        this.dataPath = storageProperties.getImagePath() + "/acg";
    }

    public GuessData initGuess(Long groupId, String category) {
        List<FilePO> characters = fileService.list(dataPath + "/" + category);
        if (characters.isEmpty()) throw new CoreException("暂无可用图片");
        FilePO character = characters.get(ThreadLocalRandom.current().nextInt(characters.size()));
        String characterName = character.getName().split("_")[0];
        GuessData guess = new GuessData(characterName, character, 0);
        guesses.put(groupId, guess);
        return guess;
    }

    public GuessData getGuess(Long groupId) {
        return guesses.getOrDefault(groupId, null);
    }

    public void removeGuess(Long groupId) {
        guesses.remove(groupId);
    }

    public void increaseTimes(Long groupId) {
        guesses.get(groupId).setTimes(guesses.get(groupId).getTimes() + 1);
    }
}
