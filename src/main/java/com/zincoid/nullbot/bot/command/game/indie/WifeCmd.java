package com.zincoid.nullbot.bot.command.game.indie;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.service.base.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
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
@CmdMapping({"Wife", "今日老婆"})
@Component
@RequiredArgsConstructor
public class WifeCmd implements Cmd {

    private final Map<Long, Long> memberWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> memberExpireMap = new ConcurrentHashMap<>();
    private final Map<Long, FilePO> acgWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> acgExpireMap = new ConcurrentHashMap<>();

    private final UserService userService;
    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        if (args.hasOpt("member", "m")) {
            int access = userService.getAccess(event.getUserId());
            if (access < 1) throw new BotWarnException("你不能用。");
            Long wifeId = args.optLong("member", "m");
            setMemberWife(bot, event, wifeId);
            return;
        }
        if (args.hasOpt("anime", "a")) {
            int access = userService.getAccess(event.getUserId());
            if (access < 1) throw new BotWarnException("你不能用。");
            String keyword = args.getOpt("anime", "a");
            setAnimeWife(bot, event, args.next(), keyword);
            return;
        }
        if (!args.hasNext()) { randomMemberWife(bot, event); }
        else { randomAnimeWife(bot, event, args); }
    }

    private void randomMemberWife(Bot bot, GroupMessageEvent event) {
        Long userId = event.getUserId();
        LocalDateTime expireTime = memberExpireMap.get(userId);
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            Long wifeId = memberWifeMap.get(userId);
            String wifeName = bot.getStrangerInfo(wifeId, true).getData().getNickname();
            String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
            String response = MsgUtils.builder()
                    .at(userId)
                    .text("""
                            \n今天已经选过了哦\uD83D\uDCA6...
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
        setMemberWife(bot, event, wife.getUserId());
    }

    private void setMemberWife(Bot bot, GroupMessageEvent event, Long wifeId) {
        Long userId = event.getUserId();
        String wifeName = bot.getStrangerInfo(wifeId, true).getData().getNickname();
        String avatarUrl = ShiroUtils.getUserAvatar(wifeId, 5);
        String response = MsgUtils.builder()
                .at(userId)
                .text("""
                        \n你的今日群友老婆是✨
                        %s(%s)""".formatted(wifeName, wifeId))
                .img(avatarUrl)
                .build();
        memberWifeMap.put(userId, wifeId);
        memberExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Wife] 今日群友老婆 - {} -> {}", userId, wifeId);
    }

    private void randomAnimeWife(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long userId = event.getUserId();
        LocalDateTime expireTime = acgExpireMap.get(userId);
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            FilePO wife = acgWifeMap.get(userId);
            String wifeName = wife.getName().split("_")[0];
            String response = MsgUtils.builder()
                    .at(userId)
                    .text("""
                            \n今天已经选过了哦\uD83D\uDCA6...
                            你的二次元老婆是
                            %s""".formatted(wifeName))
                    .img(resourceUrlBuilder.from(wife.getPath()))
                    .build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("☑ [Wife] 今日已选过二次元老婆 - {} -> {}", userId, wifeName);
            return;
        }
        setAnimeWife(bot, event, args.next(), null);
    }

    private void setAnimeWife(Bot bot, GroupMessageEvent event, String category, String keyword) {
        Long userId = event.getUserId();
        String acgPath = storageProperties.getImagePath() + "/acg/" + category;
        List<FilePO> wives = fileService.search(keyword, acgPath);
        if (wives.isEmpty())
            throw new BotInfoException(Emoji.INFO, "暂无角色");
        FilePO wife = wives.get(ThreadLocalRandom.current().nextInt(wives.size()));
        String wifeName = wife.getName().split("_")[0];
        String response = MsgUtils.builder()
                .at(userId)
                .text("""
                        \n你的今日二次元老婆是✨
                        %s - %s""".formatted(category, wifeName))
                .img(resourceUrlBuilder.from(wife.getPath()))
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
                功能: 今日老婆
                限权: %d 级
                格式: Wife [可选: 人物来源] [选项]
                
                选项:
                -m,--member=[用户ID]  指定群友老婆
                -a,--anime=[关键字]  指定二次元老婆
                
                注意:
                - 无参数时选择群友老婆, 带参数时选二次元老婆
                - 指定功能需限权I及以上
                - 以上两种选择每日均可抽一次
                - 人物来源可通过图片目录命令查看acg下子目录名获得
                别名: 今日老婆""", getAccess()
        );
    }
}
