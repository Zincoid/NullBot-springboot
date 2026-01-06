package org.bot.nullbot.command.pubg;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@CommandMapping({"PUBG", "pubg", "获取PUBG资源地图"})
@Component
@Slf4j
@RequiredArgsConstructor
public class PUBGCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(event.getCommandParameters().isEmpty()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[PUBG] ❌未指定地图", false);
                log.info("\t\t\t\t├─[PUBG] 未指定地图");
                return;
            }
            String map = switch (event.getCommandParameters().getFirst()) {
                case "艾伦格" -> "Erangel.png";
                case "米拉玛" -> "Miramar.png";
                case "维寒迪" -> "Vikendi.png";
                case "帝斯顿" -> "Deston.png";
                case "荣都" -> "Rondo.png";
                case "泰戈" -> "Tiger.png";
                default -> null;
            };
            if (map == null) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[PUBG] ❌不支持此地图", false);
                log.info("\t\t\t\t├─[PUBG] 不支持此地图");
                return;
            }
            try {
                String helpPath = ResourceLoader.getCached("static/pubg/" + map, fileStorageConfig.getTempPath()).toAbsolutePath().toString();
                String response = MsgUtils.builder().img(helpPath).build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[PUBG] 已获取资源");
            } catch (IOException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Help] ❌资源缺失", false);
                log.info("\t\t\t\t├─[PUBG] 资源缺失");
            }
        }else
            log.info("\t\t\t\t├─[PUBG] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                限权: %d 级
                格式: PUBG [地图] 或 pubg [地图]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                中文命令: 获取PUBG资源地图""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ PUBG 命令
                功能: 获取PUBG资源地图
                限权: %d 级
                格式: PUBG [地图]
                地图: 艾伦格/泰戈/帝斯顿/维寒迪/荣都/米拉玛
                示例: PUBG 帝斯顿""", getAccess()
        );
    }
}
