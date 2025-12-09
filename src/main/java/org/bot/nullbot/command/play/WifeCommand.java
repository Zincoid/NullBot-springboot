package org.bot.nullbot.command.play;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@CommandMapping({"Wife", "今日老婆"})
@Component
@RequiredArgsConstructor
public class WifeCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(WifeCommand.class);
    private final FileStorageConfig fileStorageConfig;

    private final Map<Long, Long> memberWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> memberExpireMap = new ConcurrentHashMap<>();
    private final Map<Long, String> acgWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> acgExpireMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(event.getCommandParameters().isEmpty()){
                GroupMemberInfoResp wife;
                Long userId = groupMessageEvent.getUserId();
                LocalDateTime expireTime = memberExpireMap.get(userId);
                if(expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                    List<GroupMemberInfoResp> groupMemberList = bot.getGroupMemberList(groupMessageEvent.getGroupId()).getData();
                    do {
                        int randomIndex = ThreadLocalRandom.current().nextInt(groupMemberList.size());
                        wife = groupMemberList.get(randomIndex);
                    } while (Objects.equals(wife.getUserId(), groupMessageEvent.getUserId()));
                    Long wifeId = wife.getUserId();
                    memberWifeMap.put(userId, wifeId);
                    memberExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                    String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
                    String response = MsgUtils.builder()
                            .text("✨你的今日群友老婆是\n" + wife.getNickname() + "(" + wifeId + ")")
                            .img(avatarUrl)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    logger.info("\t\t\t\t├─[Wife] 今日群友老婆: {} -> {}", userId, wifeId);
                }else{
                    Long wifeId = memberWifeMap.get(userId);
                    String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
                    String response = MsgUtils.builder()
                            .text("\uD83D\uDCA6今天已经选过了哦... 你的群友老婆是\n" + bot.getStrangerInfo(wifeId, false).getData().getNickname() + "(" + wifeId + ")")
                            .img(avatarUrl)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    logger.info("\t\t\t\t├─[Wife] 今日已选过群友老婆: {} -> {}", userId, wifeId);
                }
            }else{
                Long userId = groupMessageEvent.getUserId();
                LocalDateTime expireTime = memberExpireMap.get(userId);
                if(expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                    String category = event.getCommandParameters().get(0);
                    String acgPath = fileStorageConfig.getImagePath() + "/acg/" + category;
                    String wifePath = FileUtil.getRandomFile(acgPath);
                    String wifeName = wifePath.substring(wifePath.lastIndexOf('/') + 1,
                            wifePath.lastIndexOf('.') > wifePath.lastIndexOf('/') ?
                                    wifePath.lastIndexOf('.') : wifePath.length());
                    acgWifeMap.put(userId, wifePath);
                    acgExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                    String response = MsgUtils.builder()
                            .text("✨你的今日二次元老婆是\n" + category + " - " + wifeName)
                            .img(wifePath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    logger.info("\t\t\t\t├─[Wife] 今日二次元老婆: {} -> {}", userId, wifeName);
                }else{
                    String wifePath = acgWifeMap.get(userId);
                    String wifeName = wifePath.substring(wifePath.lastIndexOf('/') + 1,
                            wifePath.lastIndexOf('.') > wifePath.lastIndexOf('/') ?
                                    wifePath.lastIndexOf('.') : wifePath.length());
                    String response = MsgUtils.builder()
                            .text("\uD83D\uDCA6今天已经选过了哦... 你的二次元老婆是\n" + wifeName)
                            .img(wifePath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    logger.info("\t\t\t\t├─[Wife] 今日已选过二次元老婆: {} -> {}", userId, wifeName);
                }
            }
        }else
            logger.info("\t\t\t\t├─[Wife] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Wife 命令\n功能: 今日老婆(每天可抽一次)\n限权: " + getAccess() + "\n格式: Wife\n中文命令: 今日老婆";
    }
}
