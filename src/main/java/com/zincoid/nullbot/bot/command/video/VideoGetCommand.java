package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.entity.BotPageSelector;
import com.zincoid.nullbot.core.entity.po.FilePO;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@CommandMapping({"VideoGet", "获取视频", "视频检索"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoGetCommand implements Command {

    private final OssUrlBuilder ossUrlBuilder;
    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final BotInputManager botInputManager;

    private static final int PAGE_SIZE = 5;  // 查询单页大小
    private static final int WAIT_TIMEOUT = 30;  // 等待超时时间 (单位: Second)

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
                keyword, fileStorageProperties.getVideoPath() + "/" + secondary);

        if (files.isEmpty())
            throw new NullBotMsgException("[获取视频] ❌无匹配项");
        if (files.size() == 1) {
            sendVideo(bot, groupId, files.getFirst());
            return;
        }

        files.sort(Comparator.comparing(FilePO::getFileName));

        BotPageSelector<FilePO, String> pager = new BotPageSelector.Builder<>(
                bot, groupId, "视频检索", false,
                files,
                files.stream().map(FilePO::getFileName).toList(),
                this::sendVideo
        ).size(PAGE_SIZE).userId(userId).build();

        pager.init();
        while (pager.input(botInputManager, WAIT_TIMEOUT)) {
            log.info("\t\t\t\t├─[VideoGet] 已操作分页器");
        }
        // BotInputer in = new BotInputer(userId).timeout(WAIT_TIMEOUT);
        // pager.start(in);
    }

    private void sendVideo(Bot bot, Long groupId, FilePO video) {
        String response = MsgUtils.builder()
                .video(ossUrlBuilder.from(video.getId()), "")
                .build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("\t\t\t\t├─[VideoGet] 已获取视频 - {}", video.getFileName());
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
