package org.bot.nullbot.command.assist.endfield;

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@CommandMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EndfieldCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final BotNextInputer botNextInputer;

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT = 30;  // 等待超时时间 (单位: Second)
    private static final String DEFAULT_VERSION = "1.1";
    private static Map<Long, String> versions = new ConcurrentHashMap<>();  // 资源版本

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        boolean continuousQuery = false;  // 连续查询模式
        String keyword = params.isEmpty() ? "" : params.getFirst();
        if ("-c".equals(keyword)) {
            continuousQuery = true;
            if (params.size() > 1)
                keyword = params.get(1);
            else
                keyword = "";
        } else if ("-v".equals(keyword)) {
            if (params.size() > 1) setGroupVersion(groupId, params.get(1));
            bot.sendGroupMsg(groupId, "[终末地] ℹ️资源版本: " +
                    getGroupVersion(groupId), false);
            return;
        }

        List<String> helpPaths = new ArrayList<>();
        try {
            helpPaths.addAll(FileUtil.getFilePathsByKeyword(
                    fileStorageProperties.getResourcePath() + "/endfield/public", keyword));
            helpPaths.addAll(FileUtil.getFilePathsByKeyword(
                    fileStorageProperties.getResourcePath() + "/endfield/" + getGroupVersion(groupId), keyword));
        } catch (Exception e) {
            throw new NullBotMsgException("[终末地] ❌资源异常");
        }

        if (helpPaths.isEmpty())
            throw new NullBotMsgException("[终末地] ❌无查询项");

        helpPaths.sort(Comparator.naturalOrder());  // 排序
        int total = helpPaths.size();
        int pages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int current = 1;

        if (total == 1) {  // 只有一个匹配项时直接发送
            sendResource(bot, groupId, helpPaths.getFirst());
            return;
        }

        String operation = "INIT";
        while (true) {
            switch (operation) {
                case "INIT" -> sendPage(bot, groupId, helpPaths, PAGE_SIZE, current);
                case "UP" -> {
                    if (current > 1) sendPage(bot, groupId, helpPaths, PAGE_SIZE, --current);
                    else bot.sendGroupMsg(groupId, "到顶啦！", false);
                }
                case "DOWN" -> {
                    if (current < pages) sendPage(bot, groupId, helpPaths, PAGE_SIZE, ++current);
                    else bot.sendGroupMsg(groupId, "到底啦！", false);
                }
                case "END" -> {
                    bot.sendGroupMsg(groupId, "[终末地] ⛔️查询终止", false);
                    log.info("\t\t\t\t├─[Endfield] 用户 {} 查询终止", userId);
                    return;
                }
                default -> {
                    int selection;
                    try {
                        selection = Integer.parseInt(operation) - 1;
                    } catch (NumberFormatException e) {
                        throw new NullBotMsgException("[终末地] ❌格式错误");
                    }
                    if (selection < 0 || selection > total - 1)
                        throw new NullBotMsgException("[终末地] ❌范围错误");
                    sendResource(bot, groupId, helpPaths.get(selection));
                    if (!continuousQuery) return;
                }
            }

            List<Pair<Long, String>> inputs;
            try {
                inputs = botNextInputer
                        .request(BniMode.PS, userId, WAIT_TIMEOUT, "[1-9]\\d*|(?i)up|down|end");
            } catch (Exception e) {
                throw new NullBotMsgException("[终末地] ❌" + e.getMessage());
            }

            if (inputs.isEmpty())
                throw new NullBotMsgException("[终末地] ⌛️输入超时");
            operation = inputs.getFirst().getRight().toUpperCase();
        }
    }

    private void sendPage(Bot bot, Long groupId, List<String> helpPaths, int pageSize, int current) {
        int total = helpPaths.size();
        int pages = (total + pageSize - 1) / pageSize;
        int fromIndex = (current - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<String> helpNames = IntStream.range(fromIndex, toIndex)
                .mapToObj(j -> {
                    String path = helpPaths.get(j);
                    String fileNameWithExt = new File(path).getName();
                    int dotIndex = fileNameWithExt.lastIndexOf('.');
                    String fileName = dotIndex > 0 ? fileNameWithExt.substring(0, dotIndex) : fileNameWithExt;
                    return (j + 1) + ". " + fileName;
                }).toList();
        String content = String.join("\n", helpNames);
        String footer = """
                [第 %s/%s 页 (每页%s条)]
                [当前资源版本 - %s]
                操作 - Up/Down/End
                选择 - 发送序号 (上同)""".formatted(current, pages, pageSize, getGroupVersion(groupId));
        bot.sendGroupMsg(groupId, "[终末地] \uD83D\uDD0D共%s个结果\n%s\n\n%s"
                .formatted(total, content, footer), false);
        log.info("\t\t\t\t├─[Endfield] 已获取查询页 - {}/{}", current, pages);
    }

    private void sendResource(Bot bot, Long groupId, String resourcePath) {
        String helpName = new File(resourcePath).getName().toLowerCase();
        if (helpName.endsWith(".txt")) {
            try {
                String response = Files.readString(Paths.get(resourcePath), StandardCharsets.UTF_8);  // TXT文件类型 读取文本内容
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Endfield] 已获取文本内容");
            } catch (IOException e) {
                throw new NullBotMsgException("[终末地] ❌读取出错");
            }
        } else {
            String response = MsgUtils.builder().img(resourcePath).build();  // 其他文件类型 按图片处理
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Endfield] 已获取图片内容");
        }
    }

    private void setGroupVersion(Long groupId, String version) {
        versions.put(groupId, version);
    }

    private String getGroupVersion(Long groupId) {
        return versions.computeIfAbsent(groupId, k -> DEFAULT_VERSION);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Endfield 命令
                功能: 获取终末地攻略
                限权: %d 级
                格式:
                1. Endfield [可选: -c(连查模式)] [可选: 关键字]
                2. Endfield [-v] [可选: 版本]
                别名: endfield/end/终末地查询/终末地""", getAccess()
        );
    }
}
