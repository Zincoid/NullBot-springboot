package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.Base64Util;
import org.bot.nullbot.util.FileUtil;
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

    private final FileStorageProperties fileStorageProperties;

    private final Map<Long, Long> memberWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> memberExpireMap = new ConcurrentHashMap<>();
    private final Map<Long, String> acgWifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> acgExpireMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if(params.isEmpty()){
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
                        .text("今天已经选过了哦\uD83D\uDCA6...\n你的群友老婆是\n" + bot.getStrangerInfo(wifeId, true).getData().getNickname() + "(" + wifeId + ")")
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

                String wifePath;
                try {
                    wifePath = FileUtil.getRandomFilePath(acgPath);
                } catch (Exception e) {
                    throw new NullBotMsgException("[今日老婆] ❌不存在该类别"); // 目录异常
                }
                if (wifePath == null)
                    throw new NullBotMsgException("[今日老婆] ❌该类别下暂无角色");

                String wifeName = wifePath.substring(wifePath.lastIndexOf('/') + 1,
                                wifePath.lastIndexOf('.') > wifePath.lastIndexOf('/') ?
                                        wifePath.lastIndexOf('.') : wifePath.length())
                        .split("_")[0];  // 切割后缀
                acgWifeMap.put(userId, wifePath);
                acgExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                String response = MsgUtils.builder()
                        .text("你的今日二次元老婆✨是\n" + category + " - " + wifeName)
                        .img("base64://" + Base64Util.from(wifePath))
                        .build();
                bot.sendGroupMsg(event.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Wife] 今日二次元老婆: {} -> {}", userId, wifeName);
            } else {
                try {
                    String wifePath = acgWifeMap.get(userId);
                    String wifeName = wifePath.substring(wifePath.lastIndexOf('/') + 1,
                                    wifePath.lastIndexOf('.') > wifePath.lastIndexOf('/') ?
                                            wifePath.lastIndexOf('.') : wifePath.length())
                            .split("_")[0];  // 切割后缀
                    String response = MsgUtils.builder()
                            .text("今天已经选过了哦\uD83D\uDCA6...\n你的二次元老婆是\n" + wifeName)
                            .img("base64://" + Base64Util.from(wifePath))
                            .build();
                    bot.sendGroupMsg(event.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Wife] 今日已选过二次元老婆: {} -> {}", userId, wifeName);
                } catch (Exception e) {
                    throw new NullBotMsgException("[今日老婆] ❌文件已被修改");
                }
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
