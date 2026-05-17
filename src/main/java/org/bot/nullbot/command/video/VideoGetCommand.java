package org.bot.nullbot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.BotPageSelector;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@CommandMapping({"VideoGet", "获取视频", "视频检索"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoGetCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final BotNextInputer botNextInputer;

    private static final int PAGE_SIZE = 5;  // 查询单页大小
    // private static final int WAIT_TIMEOUT = 30;  // 等待超时时间 (单位: Second) 使用默认

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String secondary;
        String keyword;

        if (!params.isEmpty() && "-c".equals(params.getFirst())) {
            secondary = "collect";
            keyword = String.join(" ", params.subList(1, params.size()));
        } else {
            secondary = "storage";
            keyword = String.join(" ", params);
        }

        List<FilePO> files = fileService.search(
                keyword, fileStorageProperties.getVideoPath() + "/" + secondary).getData();

        if (files.isEmpty())
            throw new NullBotMsgException("[获取视频] ❌无匹配项");
        if (files.size() == 1) {
            sendVideo(bot, groupId, files.getFirst().getId());
            return;
        }

        files.sort(Comparator.comparing(FilePO::getFileName));
        BotPageSelector<Integer, String> pager = new BotPageSelector<>(
                bot, groupId, userId, "视频检索", "", false, PAGE_SIZE,
                files.stream().map(FilePO::getId).toList(),
                files.stream().map(FilePO::getFileName).toList(),
                this::sendVideo
        );

        pager.init();
        while (pager.input(botNextInputer)) {
            log.info("\t\t\t\t├─[VideoGet] 已操作分页器");
        }
    }

    private Void sendVideo(Bot bot, Long groupId, Integer videoId) {
        log.info("\t\t\t\t├─[VideoGet] 获取视频 - {}", videoId);
        String response = MsgUtils.builder()
                .video("http://nullbot.zincoid.online/api/preview/" + videoId, "")
                .build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("\t\t\t\t├─[VideoGet] 已获取视频 - {}", videoId);
        return null;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoGet 命令
                功能: 检索获取保存的视频
                限权: %d 级
                格式: VideoGet [可选: -c] [关键字]
                别名: 获取视频/视频检索
                注意:
                1. 默认搜索 storage 库
                2. 通过参数 [-c] 搜索 collect 库
                视频库:
                1. collect 用户收集库
                2. storage 管理存储库""", getAccess()
        );
    }
}
