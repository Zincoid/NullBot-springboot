package com.zincoid.nullbot.core.component.storage;

import lombok.Data;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.GuessInfo;
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

    private final Map<Long, GuessInfo> guesses;
    private final String dataPath;
    private final FileService fileService;

    public GuessStorage(StorageProperties storageProperties, FileService fileService) {
        guesses = new ConcurrentHashMap<>();
        dataPath = storageProperties.getImagePath() + "/acg";
        this.fileService = fileService;
    }

    public GuessInfo initGuess(Long groupId, String category) {
        List<FilePO> characters = fileService.list(dataPath + "/" + category);
        if (characters.isEmpty())
            throw new IllegalArgumentException("暂无可用图片");
        FilePO character = characters.get(ThreadLocalRandom.current().nextInt(characters.size()));
        String characterName = character.getName().split("_")[0];
        GuessInfo guess = new GuessInfo(characterName, character, 0);
        guesses.put(groupId, guess);
        return guess;
    }

    public GuessInfo getGuess(Long groupId) {
        return guesses.getOrDefault(groupId, null);
    }
    public void removeGuess(Long groupId) { guesses.remove(groupId); }

    public void increaseTimes(Long groupId) {
        guesses.get(groupId).setTimes(guesses.get(groupId).getTimes() + 1);
    }
}
