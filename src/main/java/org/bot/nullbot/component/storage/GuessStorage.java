package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.entity.info.GuessInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class GuessStorage
{
    private final Map<Long, GuessInfo> guesses = new ConcurrentHashMap<>();
    private double ratio = 0.1;
    private int padding = 250;


    public GuessInfo getGuessInfo(Long groupId) {
        return guesses.getOrDefault(groupId, null);
    }

    public void initGuessInfo(Long groupId, String characterName, String characterPath) {
        guesses.put(groupId, new GuessInfo(characterName, characterPath, 0));
    }

    public void removeGuess(Long groupId) {
        guesses.remove(groupId);
    }

    public void increaseTimes(Long groupId) {
        guesses.get(groupId).setTimes(guesses.get(groupId).getTimes() + 1);
    }
}
