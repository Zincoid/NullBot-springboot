package com.zincoid.nullbot.bot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.FileService;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import com.zincoid.nullbot.core.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@CommandMapping({"ImageDel", "删除图片"})
@Component
@RequiredArgsConstructor
public class ImageDeleteCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String directory = fileStorageProperties.getImagePath() + "/collect";
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (!args.isEmpty()) {
            deleteFile(bot, event, directory, args.nextFullString());
            return;
        }
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MsgParseUtil
                    .extractImgMap(replyMsg.getRawMessage());
            if (imageMap.isEmpty())
                throw new BotWarnException("未引用图片");
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String originName = entry.getKey();  // QQ 图片信息后缀全是 JPG
                String name = originName.substring(0, originName.lastIndexOf("."));
                List<FilePO> realFiles = fileService.search(name, directory);
                if (realFiles.size() != 1)
                    throw new BotWarnException("数据异常");
                deleteFile(bot, event, directory, realFiles.getFirst().getFileName());
            }
            return;
        }
        throw new BotWarnException("无文件名或引用");
    }

    private void deleteFile(Bot bot, GroupMessageEvent event, String directory, String fileName) {
        if (!fileService.deleteFile(directory, fileName))
            throw new BotWarnException("文件服务删除失败");
        bot.sendGroupMsg(event.getGroupId(), "[删除图片] ⚠️已删除\n- " +
                StringUtil.truncateFileName(fileName, 12), false);
        log.info("☑ [ImageDelete] 图片已删除: {}", fileName);
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageDel 命令
                功能: 删除保存的图片
                限权: %d 级
                格式:
                1. ImageDel [文件名]
                2. [引用图片] ImageDel
                别名: 删除图片""", getAccess()
        );
    }
}
