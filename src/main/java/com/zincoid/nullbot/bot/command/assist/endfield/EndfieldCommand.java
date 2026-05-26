package com.zincoid.nullbot.bot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotInputManager;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.bot.interaction.BotPageSelector;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@CommandMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@RequiredArgsConstructor
public class EndfieldCommand implements Command {

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT = 30;  // 等待超时时间 (单位: Second)
    private static final Set<String> ALLOWED_VERSIONS = Set.of("1.0", "1.1", "1.2");  // 可用资源版本
    private static final String DEFAULT_VERSION = "1.2";  // 默认资源版本

    private final Map<Long, String> versions = new ConcurrentHashMap<>();  // 群聊版本存储

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final BotInputManager botInputManager;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String version = getGroupVersion(groupId);
        boolean continuousQuery = false;  // 连续查询模式
        boolean globalQuery;  // 全局查询模式
        String keyword = params.nextStringOptional("");

        if ("-v".equals(keyword)) {
            String newVersion = params.nextString();
            if (!ALLOWED_VERSIONS.contains(newVersion))
                throw new NullBotException("版本非法");
            setGroupVersion(groupId, newVersion);
            bot.sendGroupMsg(groupId, "[终末地] \uD83D\uDD79️资源版本 - " + version, false);
            return;
        }
        if ("-c".equals(keyword)) {
            continuousQuery = true;
            keyword = params.nextStringOptional("");
        }

        List<FilePO> allFiles = new ArrayList<>();
        allFiles.addAll(fileService.search(keyword,
                fileStorageProperties.getResourcePath() + "/endfield/public"));
        allFiles.addAll(fileService.search(keyword,
                fileStorageProperties.getResourcePath() + "/endfield/" + version));

        if (allFiles.isEmpty()) {
            globalQuery = true;
            for (String ver : ALLOWED_VERSIONS) {
                allFiles.addAll(fileService.search(keyword,
                        fileStorageProperties.getResourcePath() + "/endfield/" + ver));
            }
        } else {
            globalQuery = false;
        }

        if (allFiles.isEmpty())
            throw new NullBotException("无匹配项");
        if (!globalQuery && allFiles.size() == 1) {  // 非全局查询且单匹配项时直接发送
            sendResource(bot, groupId, allFiles.getFirst());
            return;
        }

        allFiles.sort(Comparator.comparing(FilePO::getFileName));

        String info = "\n[当前资源版本 - %s]%s".formatted(
                globalQuery ? "ALL" : version,
                globalQuery ? "\n[版本 %s 无匹配资源]".formatted(version) : ""
        );

        BotPageSelector<FilePO, String> pager = BotPageSelector.builder(
                bot, groupId, "终末地", continuousQuery,
                allFiles,
                allFiles.stream().map(f -> {
                    String name = f.getName();
                    String ver = f.getDirName();
                    return globalQuery ? "[" + ver + "]" + name : name;
                }).toList(),
                this::sendResource
        ).userId(userId).info(info).size(PAGE_SIZE).build();

        // BotInputer in = new BotInputer(userId).timeout(WAIT_TIMEOUT);
        // pager.start(in);
        pager.init();
        while (pager.input(botInputManager, WAIT_TIMEOUT)) {
            log.info("☑ [Endfield] 已操作分页器");
        }
    }

    private void sendResource(Bot bot, Long groupId, FilePO file) {
        if (file.getFileName().endsWith(".txt")) {
            // TXT类型 读取文本内容
            try {
                String response = Files.readString(
                        Paths.get(file.getPath()), StandardCharsets.UTF_8);
                bot.sendGroupMsg(groupId, response, false);
                log.info("☑ [Endfield] 已获取文本 - {}", file.getFileName());
            } catch (IOException e) {
                throw new NullBotException("[终末地] ❌读取出错");
            }
        } else {
            // 其他类型 暂时图片处理
            String response = MsgUtils.builder()
                    .img(ossUrlBuilder.from(file.getId()))
                    .build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("☑ [Endfield] 已获取图片 - {}", file.getFileName());
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
