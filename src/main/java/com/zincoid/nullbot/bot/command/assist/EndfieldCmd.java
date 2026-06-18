package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.bot.interactor.BotPageSelector;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@CmdMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@RequiredArgsConstructor
public class EndfieldCmd implements Cmd {

    private static final int PAGE_SIZE = 10;  // 查询单页大小
    private static final int WAIT_TIMEOUT_SECONDS = 30;  // 等待超时时间
    private static Set<String> ALLOWED_VERSIONS;  // 可用资源版本
    private static String DEFAULT_VERSION;  // 默认资源版本

    private final Map<Long, String> versions = new ConcurrentHashMap<>();  // 群聊版本存储

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final BotInputManager botInputManager;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @PostConstruct
    public void init() {
        ALLOWED_VERSIONS = fileService.list(storageProperties.getResourcePath() + "/endfield").stream()
                .map(FilePO::getFileName).collect(Collectors.toSet());
        DEFAULT_VERSION = ALLOWED_VERSIONS.stream()
                .filter(v -> Character.isDigit(v.charAt(0)))
                .max(Comparator.naturalOrder()).orElse("public");
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String endfieldPath = storageProperties.getResourcePath() + "/endfield";
        String curVersion = versions.computeIfAbsent(groupId, k -> DEFAULT_VERSION);
        String keyword = args.nextString("");

        if ("--version".equals(keyword) || "-v".equals(keyword)) {
            String newVersion = args.nextString();
            if (!ALLOWED_VERSIONS.contains(newVersion))
                throw new BotWarnException("版本非法");
            versions.put(groupId, newVersion);
            bot.sendGroupMsg(groupId, "\uD83D\uDD79️版本已切换", false);
            return;
        }

        if ("--reload".equals(keyword) || "-r".equals(keyword)) {
            init();
            versions.remove(groupId);
            bot.sendGroupMsg(groupId, "\uD83D\uDD79️版本已更新", false);
            return;
        }

        boolean continuousQuery = false;
        if ("--continuous".equals(keyword) || "-c".equals(keyword)) {
            continuousQuery = true;
            keyword = args.nextString("");
        }
        List<FilePO> allFiles = new ArrayList<>();
        allFiles.addAll(fileService.search(keyword, endfieldPath + "/public"));
        allFiles.addAll(fileService.search(keyword, endfieldPath + "/" + curVersion));
        boolean globalQuery = allFiles.isEmpty();
        if (globalQuery)
            for (String version : ALLOWED_VERSIONS)
                allFiles.addAll(fileService.search(keyword, endfieldPath + "/" + version));
        if (allFiles.isEmpty())
            throw new BotInfoException(Emoji.INFO, "无匹配项");

        if (!globalQuery && allFiles.size() == 1) {
            sendResource(bot, groupId, allFiles.getFirst());
            return;
        }

        allFiles.sort(Comparator.comparing(FilePO::getFileName));

        String info = "\n[当前资源版本 - %s]%s".formatted(
                globalQuery ? "ALL" : curVersion,
                globalQuery ? "\n[版本 %s 无匹配资源]".formatted(curVersion) : ""
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
        pager.init();
        while (pager.input(botInputManager, WAIT_TIMEOUT_SECONDS)) {
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
                log.info("☑ [Endfield] 文本已获取: {}", file.getFileName());
            } catch (IOException e) {
                throw new BotWarnException("读取出错");
            }
        } else {
            // 其他类型 暂时图片处理
            String response = MsgUtils.builder()
                    .img(resourceUrlBuilder.from(file.getId()))
                    .build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("☑ [Endfield] 图片已获取: {}", file.getFileName());
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Endfield 命令
                功能: 获取终末地攻略
                限权: %d 级
                用法: Endfield [选项] [关键字]

                选项:
                  -c, --continuous     启用连查模式
                  -v, --version [版本]  切换资源版本
                  -r, --reload         更新版本目录

                别名: endfield/end/终末地查询/终末地""", getAccess()
        );
    }
}
