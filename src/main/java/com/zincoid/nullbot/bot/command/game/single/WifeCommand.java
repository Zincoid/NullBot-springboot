package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.FileService;
import com.zincoid.nullbot.core.util.Base64Util;
import org.springframework.stereotype.Component;

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
@Slf4j
public class WifeCommand implements Command {

    private final Map<Long, Long> memberWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> memberExpireMap = new ConcurrentHashMap<>();
    private final Map<Long, FilePO> acgWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> acgExpireMap = new ConcurrentHashMap<>();

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty()) {
            GroupMemberInfoResp wife;
            Long userId = event.getUserId();
            LocalDateTime expireTime = memberExpireMap.get(userId);
            if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                List<GroupMemberInfoResp> groupMemberList = bot.getGroupMemberList(event.getGroupId()).getData();
                do {
                    int randomIndex = ThreadLocalRandom.current().nextInt(groupMemberList.size());
                    wife = groupMemberList.get(randomIndex);
                } while (Objects.equals(wife.getUserId(), event.getUserId()));
                Long wifeId = wife.getUserId();
                memberWifeMap.put(userId, wifeId);
                memberExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
                String response = MsgUtils.builder()
                        .text("你的今日群友老婆✨是\n" + wife.getNickname() + "(" + wifeId + ")")
                        .img(avatarUrl)
                        .build();
                bot.sendGroupMsg(event.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Wife] 今日群友老婆: {} -> {}", userId, wifeId);
            } else {
                Long wifeId = memberWifeMap.get(userId);
                String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
                String response = MsgUtils.builder()
                        .text("今天已经选过了哦\uD83D\uDCA6...\n你的群友老婆是\n" + bot.getStrangerInfo(
                                wifeId, true).getData().getNickname() + "(" + wifeId + ")")
                        .img(avatarUrl)
                        .build();
                bot.sendGroupMsg(event.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Wife] 今日已选过群友老婆: {} -> {}", userId, wifeId);
            }
        } else {
            Long userId = event.getUserId();
            LocalDateTime expireTime = acgExpireMap.get(userId);
            if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                String category = params.getFirst();
                String acgPath = fileStorageProperties.getImagePath() + "/acg/" + category;

                List<FilePO> wives = fileService.search("", acgPath);
                if (wives.isEmpty())
                    throw new NullBotMsgException("[今日老婆] ❌暂无角色");
                FilePO wife = wives.get(ThreadLocalRandom.current().nextInt(wives.size()));
                String wifeName = wife.getName().split("_")[0];
                acgWifeMap.put(userId, wife);
                acgExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                String response = MsgUtils.builder()
                        .text("你的今日二次元老婆✨是\n" + category + " - " + wifeName)
                        .img("base64://" + Base64Util.from(wife.getPath()))
                        .build();
                bot.sendGroupMsg(event.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Wife] 今日二次元老婆: {} -> {}", userId, wifeName);
            } else {
                FilePO wife = acgWifeMap.get(userId);
                String wifeName = wife.getName().split("_")[0];
                String response = MsgUtils.builder()
                        .text("今天已经选过了哦\uD83D\uDCA6...\n你的二次元老婆是\n" + wifeName)
                        .img("base64://" + Base64Util.from(wife.getPath()))
                        .build();
                bot.sendGroupMsg(event.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Wife] 今日已选过二次元老婆: {} -> {}", userId, wifeName);
            }
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Wife 命令
                功能: 今日老婆(每日可抽一次)
                限权: %d 级
                格式: Wife [可选: 人物来源]
                注意:
                - 无参数时选择群友老婆
                - 带参数时选二次元老婆
                - 以上两种每日均可抽一次
                - 人物来源可通过图片目录命令查看acg下子目录名获得
                别名: 今日老婆""", getAccess()
        );
    }
}
