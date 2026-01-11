package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.component.convert.ImageConverter;
import org.springframework.stereotype.Component;

import java.util.*;

@CommandMapping({"Convert", "图像处理"})
@Component
@Slf4j
@RequiredArgsConstructor
public class ConvertCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;
    private final ImageConverter imageConverter;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();

            if (event.getCommandParameters().isEmpty()) {
                bot.sendGroupMsg(groupId, "[图像处理] ❌无方法参数", false);
                log.info("\t\t\t\t├─[Convert] 无方法参数");
                return;
            }

            String method = event.getCommandParameters().getFirst();
            if (!List.of("RIP", "PRTS", "InversePRTS").contains(method)) {
                bot.sendGroupMsg(groupId, "[图像处理] ❌方法不存在", false);
                log.info("\t\t\t\t├─[Convert] 方法不存在");
                return;
            }

            List<String> urls = new ArrayList<>();

            // 引用收集
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                urls.addAll(imageMap.values());
            }

            //  ID参数收集 或 AT收集
            if (event.getCommandParameters().size() > 1) {
                long qqNumber;
                try {
                    qqNumber = Long.parseLong(event.getCommandParameters().get(1));
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupId, "[图像处理] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[Convert] 参数格式错误");
                    return;
                }
                urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }else{
                List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
                for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }

            if (urls.isEmpty()) {
                bot.sendGroupMsg(groupId, "[图像处理] ❌无引用图片或ID参数或At消息", false);
                log.info("\t\t\t\t├─[Convert] 无引用图片或ID参数或At消息");
                return;
            }

            // 开始处理
            String tempFilePath = fileStorageConfig.getTempPath();
            for (String url : urls) {
                String tempFileName = UUID.randomUUID().toString();
                String downloadedFileName;
                try {
                    FileInfo fileInfo = DownloadUtil.downloadFile(url, tempFilePath, tempFileName);
                    downloadedFileName = fileInfo.getFileName();
                } catch (Exception e) {
                    bot.sendGroupMsg(groupId, "[图像处理] ❌下载图像失败", false);
                    log.info("\t\t\t\t├─[Convert] 下载图像失败 - {}", url);
                    continue;
                }
                String avatarPath = tempFilePath + "/" + downloadedFileName;
                try {
                    String base64 = switch (method){
                        case "RIP" -> imageConverter.RIP(avatarPath, tempFilePath + "/fonts");
                        case "PRTS" -> imageConverter.PRTS(avatarPath, tempFilePath + "/fonts");
                        case "InversePRTS" -> imageConverter.inversePRTS(avatarPath, tempFilePath + "/fonts");
                        default -> throw new IllegalStateException("Unexpected value: " + method);
                    };
                    String response = MsgUtils.builder().img("base64://" + base64).build();
                    bot.sendGroupMsg(groupId, response, false);
                    log.info("\t\t\t\t├─[Convert] 处理完成 - {}", downloadedFileName);
                } catch (Exception e) {
                    bot.sendGroupMsg(groupId, "[图像处理] ❌处理时出错", false);
                    log.info("\t\t\t\t├─[Convert] 处理时出错 - {}", e.getMessage());
                } finally {
                    FileUtil.deleteFileByName(tempFilePath, downloadedFileName);
                }
            }
            // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图像处理] ✅全部处理完成！", false);
        }else
            log.info("\t\t\t\t├─[Convert] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Convert 命令
                功能: P图!!!
                方式: RIP/PRTS/InversePRTS...更多开发中
                限权: %d 级
                格式: [引用] Convert [处理方式]
                或 Convert [处理方式] [@任何人/QQ号]
                中文命令: 图像处理""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Convert 命令
                功能: P图!!!
                方式: RIP(安息)/PRTS(封锁)/InversePRTS(封锁反色)
                限权: %d 级
                格式: Convert [方式] [QQ号]
                示例: Convert RIP 2660181154""", getAccess()
        );
    }
}
