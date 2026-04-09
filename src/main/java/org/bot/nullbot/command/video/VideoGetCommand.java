package org.bot.nullbot.command.video;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@CommandMapping({"VideoGet", "获取视频", "视频检索"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoGetCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final BotNextInputer botNextInputer;

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

        // String videoPath = FileUtil.getFilePathByName(fileStorageProperties.getVideoPath(), params.getFirst());
        List<String> videoPaths = new ArrayList<>(FileUtil.getFilePathsByKeyword(
                fileStorageProperties.getVideoPath() + "/" + secondary,
                keyword)
        );

        if (videoPaths.isEmpty())
            throw new NullBotMsgException("[获取视频] ❌无匹配项");
        if (videoPaths.size() == 1) {
            sendVideo(bot, groupId, videoPaths.getFirst());
            return;
        }

        videoPaths.sort(Comparator.naturalOrder());  // 排序
        int total = videoPaths.size();
        int pages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int current = 1;

        String operation = "INIT";
        while (true) {
            switch (operation) {
                case "INIT" -> sendPage(bot, groupId, videoPaths, PAGE_SIZE, current);
                case "UP" -> {
                    if (current > 1) sendPage(bot, groupId, videoPaths, PAGE_SIZE, --current);
                    else bot.sendGroupMsg(groupId, "到顶啦！", false);
                }
                case "DOWN" -> {
                    if (current < pages) sendPage(bot, groupId, videoPaths, PAGE_SIZE, ++current);
                    else bot.sendGroupMsg(groupId, "到底啦！", false);
                }
                case "END" -> {
                    bot.sendGroupMsg(groupId, "[获取视频] ⛔️查询终止", false);
                    log.info("\t\t\t\t├─[VideoGet] 用户 {} 查询终止", userId);
                    return;
                }
                default -> {
                    int selection;
                    try {
                        selection = Integer.parseInt(operation) - 1;
                    } catch (NumberFormatException e) {
                        throw new NullBotMsgException("[获取视频] ❌格式错误");
                    }
                    if (selection < 0 || selection > total - 1)
                        throw new NullBotMsgException("[获取视频] ❌范围错误");
                    sendVideo(bot, groupId, videoPaths.get(selection));
                    return;
                }
            }

            List<Pair<Long, String>> inputs;
            try {
                inputs = botNextInputer
                        .request(BniMode.PS, userId, "[1-9]\\d*|(?i)up|down|end", WAIT_TIMEOUT);
            } catch (Exception e) {
                throw new NullBotMsgException("[获取视频] ❌" + e.getMessage());
            }

            if (inputs.isEmpty())
                throw new NullBotMsgException("[获取视频] ⌛️输入超时");
            operation = inputs.getFirst().getRight().toUpperCase();
        }
    }

    private void sendPage(Bot bot, Long groupId, List<String> helpPaths,
                          int pageSize, int current) {
        int total = helpPaths.size();
        int pages = (total + pageSize - 1) / pageSize;
        int fromIndex = (current - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<String> helpNames = IntStream.range(fromIndex, toIndex)
                .mapToObj(i -> {
                    String[] split = helpPaths.get(i).split("/");
                    String fileName = split[split.length - 1];
                    String fileVer = split[split.length - 2];
                    int dotIndex = fileName.lastIndexOf('.');
                    String name = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
                    return (i + 1) + ". " + name;
                }).toList();
        String content = String.join("\n", helpNames);
        String footer = """
                [第 %s/%s 页 (每页%s条)]
                操作 - Up/Down/End
                选择 - 发送序号 (上同)"""
                .formatted(current, pages, pageSize);
        bot.sendGroupMsg(groupId, "[视频检索] \uD83D\uDD0D共%s个结果\n%s\n\n%s"
                .formatted(total, content, footer), false);
        log.info("\t\t\t\t├─[VideoGet] 已获取查询页 - {}/{}", current, pages);
    }

    private void sendVideo(Bot bot, Long groupId, String videoPath) {
        String response = MsgUtils.builder()
                .video(videoPath, "")
                .build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("\t\t\t\t├─[VideoGet] 已获取视频 - {}", videoPath);
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
