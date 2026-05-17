package org.bot.nullbot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.component.tool.OssUrlBuilder;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.BotPageSelector;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CommandMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EndfieldCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final BotNextInputer botNextInputer;

    private final Map<Long, String> versions = new ConcurrentHashMap<>();  // 群聊版本存储

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    // private static final int WAIT_TIMEOUT = 30;  // 等待超时时间 (单位: Second) 使用默认
    private static final Set<String> ALLOWED_VERSIONS = Set.of("1.0", "1.1", "1.2");  // 可用资源版本
    private static final String DEFAULT_VERSION = "1.2";  // 默认资源版本
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String version = getGroupVersion(groupId);
        boolean continuousQuery = false;  // 连续查询模式
        boolean globalQuery;  // 全局查询模式
        String keyword = params.isEmpty() ? "" : params.getFirst();

        if ("-c".equals(keyword)) {
            continuousQuery = true;
            if (params.size() > 1) {
                keyword = params.get(1);
            } else {
                keyword = "";
            }
        } else if ("-v".equals(keyword)) {
            if (params.size() > 1) {
                String newVersion = params.get(1);
                if (!ALLOWED_VERSIONS.contains(newVersion))
                    throw new NullBotMsgException("[终末地] ❌版本非法");
                setGroupVersion(groupId, newVersion);
            }
            bot.sendGroupMsg(groupId, "[终末地] \uD83D\uDD79当前️资源版本 - " + version, false);
            return;
        }

        List<FilePO> allFiles = new ArrayList<>();
        allFiles.addAll(fileService.search(keyword,
                fileStorageProperties.getResourcePath() + "/endfield/public").getData());
        allFiles.addAll(fileService.search(keyword,
                fileStorageProperties.getResourcePath() + "/endfield/" + version).getData());

        if (allFiles.isEmpty()) {
            globalQuery = true;
            for (String ver : ALLOWED_VERSIONS) {
                allFiles.addAll(fileService.search(keyword,
                        fileStorageProperties.getResourcePath() + "/endfield/" + ver).getData());
            }
        } else {
            globalQuery = false;
        }

        if (allFiles.isEmpty())
            throw new NullBotMsgException("[终末地] ❌无匹配项");
        if (!globalQuery && allFiles.size() == 1) {  // 非全局查询且单匹配项时直接发送
            sendResource(bot, groupId, allFiles.getFirst());
            return;
        }

        allFiles.sort(Comparator.comparing(FilePO::getFileName));

        String info = "\n[当前资源版本 - %s]%s".formatted(
                globalQuery ? "ALL" : version,
                globalQuery ? "\n[版本 %s 无匹配资源]".formatted(version) : ""
        );
        BotPageSelector<FilePO, String> pager = new BotPageSelector<>(
                bot, groupId, userId, "终末地", info, continuousQuery, PAGE_SIZE,
                allFiles,
                allFiles.stream().map(f -> {
                    String name = f.getName();
                    String ver = f.getDirName();
                    return globalQuery ? "[" + ver + "]" + name : name;
                }).toList(),
                this::sendResource
        );

        pager.init();
        while (pager.input(botNextInputer)) {
            log.info("\t\t\t\t├─[Endfield] 已操作分页器");
        }
    }

    private Void sendResource(Bot bot, Long groupId, FilePO file) {
        if (file.getFileName().endsWith(".txt")) {  // TXT文件类型 读取文本内容
            try {
                String response = Files.readString(
                        Paths.get(file.getPath()), StandardCharsets.UTF_8);
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Endfield] 已获取文本内容");
            } catch (IOException e) {
                throw new NullBotMsgException("[终末地] ❌读取出错");
            }
        } else {  // 其他文件类型 暂时按图片处理
            String response = MsgUtils.builder()
                    .img(ossUrlBuilder.from(file.getId()))
                    .build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Endfield] 已获取图片内容");
        }
        return null;
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
