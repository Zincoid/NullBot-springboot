package com.zincoid.nullbot.bot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.bot.interactor.BotPageSelector;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@CommandMapping({"VideoGet", "获取视频", "视频检索"})
@Component
@RequiredArgsConstructor
public class VideoGetCommand implements Command {

    private static final int WAIT_TIMEOUT_SECONDS = 30;  // 等待超时时间
    private static final int PAGE_SIZE = 5;  // 查询单页大小

    private final ResourceUrlBuilder resourceUrlBuilder;
    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final BotInputManager botInputManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String secondary;
        String keyword;

        if (args.hasNext() && "-c".equals(args.getString(0))) {
            secondary = "collect";
            keyword = args.getFullString(1);
        } else {
            secondary = "storage";
            keyword = args.getFullString(0, "");
        }

        List<FilePO> files = fileService.search(
                keyword, storageProperties.getVideoPath() + "/" + secondary);

        if (files.isEmpty())
            throw new BotInfoException(Emoji.INFO, "无匹配项");
        if (files.size() == 1) {
            sendVideo(bot, groupId, files.getFirst());
            return;
        }

        files.sort(Comparator.comparing(FilePO::getFileName));

        BotPageSelector<FilePO, String> pager = BotPageSelector.builder(
                bot, groupId, "视频检索", false,
                files,
                files.stream().map(FilePO::getFileName).toList(),
                this::sendVideo
        ).size(PAGE_SIZE).userId(userId).build();

        pager.init();
        while (pager.input(botInputManager, WAIT_TIMEOUT_SECONDS)) {
            log.info("☑ [VideoGet] 已操作分页器");
        }
    }

    private void sendVideo(Bot bot, Long groupId, FilePO video) {
        String response = MsgUtils.builder()
                .video(resourceUrlBuilder.from(video.getId()), "")
                .build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [VideoGet] 视频已获取: {}", video.getFileName());
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
