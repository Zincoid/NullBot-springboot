package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.GuessInfo;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class GuessStorage {

    private final Map<Long, GuessInfo> guesses;
    private final String dataPath;

    public GuessStorage(FileStorageProperties fileStorageProperties) {
        guesses = new ConcurrentHashMap<>();
        dataPath = fileStorageProperties.getImagePath() + "/acg";
    }

    public GuessInfo initGuess(Long groupId, String category) {
        String characterPath;
        try {
            characterPath = FileUtil.getRandomFilePath(dataPath + "/" + category);
        } catch (Exception e) {
            throw new IllegalArgumentException("该类别不存在");  // 目录异常
        }
        if (characterPath == null)
            throw new IllegalArgumentException("该类别下暂无图片");
        String characterName = characterPath
                .split("/")[characterPath.split("/").length-1]
                .split("_")[0];
        GuessInfo guess = new GuessInfo(characterName, characterPath, 0);
        guesses.put(groupId, guess);
        return guess;
    }

    public GuessInfo getGuess(Long groupId) {
        return guesses.getOrDefault(groupId, null);
    }
    public GuessInfo removeGuess(Long groupId) { return guesses.remove(groupId); }

    public void increaseTimes(Long groupId) {
        guesses.get(groupId).setTimes(guesses.get(groupId).getTimes() + 1);
    }
}
