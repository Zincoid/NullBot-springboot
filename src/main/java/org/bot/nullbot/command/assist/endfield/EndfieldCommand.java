package org.bot.nullbot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@CommandMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EndfieldCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();

            if (params.isEmpty() || "-list".equals(params.getFirst())) {
                String imageList = FileUtil.getFileListAsString(fileStorageProperties.getImagePath() + "/assist/endfield", "\n- ", false);
                bot.sendGroupMsg(groupId, "[终末地] \uD83D\uDD0D可查询项\n- " + imageList, false);
                log.info("\t\t\t\t├─[Endfield] 已获取列表");
                return;
            }

            List<String> helpPaths = FileUtil.getFilesByKeyword(fileStorageProperties.getImagePath() + "/assist/endfield", params.getFirst());
            if (helpPaths.isEmpty()) throw new NullBotMsgException("[终末地] ❌无查询项");
            if (helpPaths.size() > 1) {
                List<String> helpNames = helpPaths.stream()
                        .map(path -> {
                            String fileNameWithExt = new File(path).getName();
                            int dotIndex = fileNameWithExt.lastIndexOf('.');
                            return dotIndex > 0 ? fileNameWithExt.substring(0, dotIndex) : fileNameWithExt;
                        }).toList();
                String helpList = String.join("\n- ", helpNames);
                bot.sendGroupMsg(groupId, "[终末地] \uD83D\uDD0D共" + helpPaths.size() + "个结果 请指定\n- " + helpList, false);
                log.info("\t\t\t\t├─[Endfield] 找到 {} 个匹配项", helpPaths.size());
                return;
            }

            String helpPath = helpPaths.getFirst();
            String helpName = new File(helpPath).getName().toLowerCase();
            if (helpName.endsWith(".txt")) {
                // TXT文件类型 读取文本内容
                try {
                    String response = Files.readString(Paths.get(helpPath), StandardCharsets.UTF_8);
                    bot.sendGroupMsg(groupId, response, false);
                    log.info("\t\t\t\t├─[Endfield] 已获取文本内容");
                } catch (IOException e) {
                    throw new NullBotMsgException("[终末地] ❌文本读取出错");
                }
            } else {
                // 其他文件类型 按图片处理
                String response = MsgUtils.builder().img(helpPath).build();
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Endfield] 已获取图片内容");
            }
        }else
            throw new NullBotLogException("[终末地] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Endfield 命令
                功能: 获取终末地攻略
                限权: %d 级
                格式: Endfield [可选: 关键字|-list]
                别名: endfield/end/终末地查询/终末地""", getAccess()
        );
    }
}
