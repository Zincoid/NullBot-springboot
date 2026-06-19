package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"PUBG", "PUBG地图"})
@Component
@RequiredArgsConstructor
public class PUBGCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String map = switch (args.next()) {
            case "艾伦格" -> "Erangel.png";
            case "米拉玛" -> "Miramar.png";
            case "维寒迪" -> "Vikendi.png";
            case "帝斯顿" -> "Deston.png";
            case "荣都" -> "Rondo.png";
            case "泰戈" -> "Tiger.png";
            default -> throw new BotWarnException("暂不支持");
        };
        String helpPath = storageProperties.getResourcePath() + "/pubg";
        List<FilePO> helps = fileService.search(map, helpPath);
        if (helps.isEmpty()) throw new BotErrorException("资源缺失");
        String response = MsgUtils.builder()
                .img(resourceUrlBuilder.from(helps.getFirst().getId()))
                .build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [PUBG] 地图已获取: {}", map);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                限权: %d 级
                格式: PUBG [地图]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                别名: PUBG地图""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                格式: PUBG [地图]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                示例: PUBG 帝斯顿""";
    }
}
