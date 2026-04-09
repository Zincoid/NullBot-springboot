package org.bot.nullbot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@CommandMapping({"VideoSave", "保存视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoSaveCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new NullBotMsgException("[保存视频] ❌需引用视频");

        GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
        // 可优化为单个键值对?
        Map<String, String> videoMap = MessageParseUtil.parseGroupRawMessageAsVideoMap(replyMsg.getRawMessage());
        if(videoMap.isEmpty())
            throw new NullBotMsgException("[保存视频] ❌未包含视频");
        if(videoMap.size() > 1)
            throw new NullBotMsgException("[保存视频] ❌视频数过多");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        Map.Entry<String, String> entry = videoMap.entrySet().iterator().next();
        String fileName = params.isEmpty() ? entry.getKey()
                : String.join(" ", params) + "." + entry.getKey().split("\\.")[1];
        if (fileName.matches(".*[\\\\/:*?\"<>|].*"))
            throw new NullBotMsgException("[保存视频] ❌文件名非法");
        String filePath = fileStorageProperties.getVideoPath();
        String url = entry.getValue();
        try {
            FileInfo fileInfo = DownloadUtil.downloadFile(url, filePath, fileName, "\t\t\t\t├─ ");
            if(!fileService.addFileRecordForBot(
                    filePath,
                    fileInfo.getFileName(),
                    fileInfo.getFileSize(),
                    fileInfo.getLastModified(),
                    userId, userName)
            ) {
                throw new NullBotMsgException("[保存视频] ❌数据库更新失败");
            }
            bot.sendGroupMsg(groupId, "\uD83C\uDFA5 已保存！", false);
            log.info("\t\t\t\t├─[VideoSave] 已保存 - {}", fileInfo.getFileName());
        } catch (Exception e) {
            throw new NullBotMsgException("[保存视频] ❌出错: " + e.getMessage());
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoSave 命令
                功能: 保存视频至视频库
                限权: %d 级
                格式: [引用视频] VideoSave [可选: 文件名]
                别名: 保存视频""", getAccess()
        );
    }
}
