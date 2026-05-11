package org.bot.nullbot.command.assist.pubg;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"PUBG", "pubg", "资源地图"})
@Component
@Slf4j
@RequiredArgsConstructor
public class PUBGCommand implements Command {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[PUBG] ❌未指定地图");
        String map = switch (params.getFirst()) {
            case "艾伦格" -> "Erangel.png";
            case "米拉玛" -> "Miramar.png";
            case "维寒迪" -> "Vikendi.png";
            case "帝斯顿" -> "Deston.png";
            case "荣都" -> "Rondo.png";
            case "泰戈" -> "Tiger.png";
            default -> null;
        };
        if (map == null)
            throw new NullBotMsgException("[PUBG] ❌不支持此地图");
        String helpPath = FileUtil.getFilePathByName(fileStorageProperties.getResourcePath() + "/pubg", map);
        if (helpPath == null)
            throw new NullBotMsgException("[PUBG] ❌资源缺失");
        String response = MsgUtils.builder().img(helpPath).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("\t\t\t\t├─[PUBG] 已获取地图");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                限权: %d 级
                格式: PUBG [地图名]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                别名: pubg/资源地图""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                格式: PUBG [地图名]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                示例: PUBG 帝斯顿""";
    }
}
