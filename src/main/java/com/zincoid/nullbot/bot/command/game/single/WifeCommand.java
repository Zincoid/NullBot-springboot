package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
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

@Slf4j
@CommandMapping({"Wife", "今日老婆"})
@Component
@RequiredArgsConstructor
public class WifeCommand implements Command {

    private final Map<Long, Long> memberWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> memberExpireMap = new ConcurrentHashMap<>();
    private final Map<Long, FilePO> acgWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> acgExpireMap = new ConcurrentHashMap<>();

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        if (args.hasNext()) {
            animeWife(bot, event, args);
        } else {
            memberWife(bot, event);
        }
    }

    private void memberWife(Bot bot, GroupMessageEvent event) {
        Long userId = event.getUserId();
        LocalDateTime expireTime = memberExpireMap.get(userId);
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            Long wifeId = memberWifeMap.get(userId);
            String wifeName = bot.getStrangerInfo(wifeId, true).getData().getNickname();
            String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
            String response = MsgUtils.builder()
                    .text("""
                            今天已经选过了哦\uD83D\uDCA6...
                            你的群友老婆是
                            %s(%s)""".formatted(wifeName, wifeId))
                    .img(avatarUrl)
                    .build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("☑ [Wife] 今日已选过群友老婆 - {} -> {}", userId, wifeId);
            return;
        }
        List<GroupMemberInfoResp> members = bot.getGroupMemberList(event.getGroupId()).getData();
        GroupMemberInfoResp wife;
        do {
            int randomIndex = ThreadLocalRandom.current().nextInt(members.size());
            wife = members.get(randomIndex);
        } while (Objects.equals(wife.getUserId(), event.getUserId()));
        Long wifeId = wife.getUserId();
        String wifeName = wife.getNickname();
        String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
        String response = MsgUtils.builder()
                .text("""
                        你的今日群友老婆是✨
                        %s(%s)""".formatted(wifeName, wifeId))
                .img(avatarUrl)
                .build();
        memberWifeMap.put(userId, wifeId);
        memberExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Wife] 今日群友老婆 - {} -> {}", userId, wifeId);
    }

    private void animeWife(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long userId = event.getUserId();
        LocalDateTime expireTime = acgExpireMap.get(userId);
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            FilePO wife = acgWifeMap.get(userId);
            String wifeName = wife.getName().split("_")[0];
            String response = MsgUtils.builder()
                    .text("""
                            今天已经选过了哦\uD83D\uDCA6...
                            你的二次元老婆是
                            %s""".formatted(wifeName))
                    .img("base64://" + Base64Util.from(wife.getPath()))
                    .build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("☑ [Wife] 今日已选过二次元老婆 - {} -> {}", userId, wifeName);
            return;
        }
        String category = args.nextString();
        String acgPath = fileStorageProperties.getImagePath() + "/acg/" + category;
        List<FilePO> wives = fileService.search("", acgPath);
        if (wives.isEmpty())
            throw new BotInfoException(Emoji.INFO, "暂无角色");
        FilePO wife = wives.get(ThreadLocalRandom.current().nextInt(wives.size()));
        String wifeName = wife.getName().split("_")[0];
        String response = MsgUtils.builder()
                .text("""
                        你的今日二次元老婆是✨
                        %s - %s""".formatted(category, wifeName))
                .img("base64://" + Base64Util.from(wife.getPath()))
                .build();
        acgWifeMap.put(userId, wife);
        acgExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Wife] 今日二次元老婆 - {} -> {}", userId, wifeName);
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
