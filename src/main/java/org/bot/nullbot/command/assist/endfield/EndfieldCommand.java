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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CommandMapping({"Endfield", "endfield", "end", "终末地查询", "终末地"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EndfieldCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final BotNextInputer botNextInputer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String keyword = params.isEmpty() ? "" : params.getFirst();

        List<String> helpPaths = new ArrayList<>(FileUtil.getFilePathsByKeyword(
                fileStorageProperties.getResourcePath() + "/endfield",
                keyword
        ));
        helpPaths.sort(Comparator.naturalOrder());  // 排序
        if (helpPaths.isEmpty())
            throw new NullBotMsgException("[终末地] ❌无查询项");
        int i = 0;
        if (helpPaths.size() > 1) {
            List<String> helpNames = IntStream.range(0, helpPaths.size())
                    .mapToObj(j -> {
                        String path = helpPaths.get(j);
                        String fileNameWithExt = new File(path).getName();
                        int dotIndex = fileNameWithExt.lastIndexOf('.');
                        String fileName = dotIndex > 0 ? fileNameWithExt.substring(0, dotIndex) : fileNameWithExt;
                        return (j + 1) + ". " + fileName;
                    }).toList();
            String helpList = String.join("\n", helpNames);
            bot.sendGroupMsg(groupId, """
                        [终末地] \uD83D\uDD0D共%s个结果
                        %s
                        
                        请发送序号来选择内容""".formatted(helpPaths.size(), helpList), false);
            log.info("\t\t\t\t├─[Endfield] 找到 {} 个匹配项", helpPaths.size());

            List<Pair<Long, String>> inputs;
            try {
                inputs = botNextInputer.request(BniMode.PS, userId, 20, "[1-9]\\d*");
            } catch (Exception e) {
                throw new NullBotMsgException("[终末地] ❌" + e.getMessage());
            }
            if (inputs.isEmpty())
                throw new NullBotMsgException("[终末地] ⌛️输入超时");
            try {
                i = Integer.parseInt(inputs.getFirst().getRight()) - 1;
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[终末地] ❌格式错误");
            }
            if (i < 0 || i > helpPaths.size() - 1)
                throw new NullBotMsgException("[终末地] ❌范围错误");
        }

        String helpPath = helpPaths.get(i);
        String helpName = new File(helpPath).getName().toLowerCase();
        if (helpName.endsWith(".txt")) {
            // TXT文件类型 读取文本内容
            try {
                String response = Files.readString(Paths.get(helpPath), StandardCharsets.UTF_8);
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Endfield] 已获取文本内容");
            } catch (IOException e) {
                throw new NullBotMsgException("[终末地] ❌读取出错");
            }
        } else {
            // 其他文件类型 按图片处理
            String response = MsgUtils.builder().img(helpPath).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Endfield] 已获取图片内容");
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Endfield 命令
                功能: 获取终末地攻略
                限权: %d 级
                格式: Endfield [可选: 关键字]
                别名: endfield/end/终末地查询/终末地""", getAccess()
        );
    }
}
