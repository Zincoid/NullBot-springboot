package com.zincoid.nullbot.command.image;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.config.prop.FileStorageProperties;
import com.zincoid.nullbot.entity.po.FilePO;
import com.zincoid.nullbot.exception.NullBotMsgException;
import com.zincoid.nullbot.service.FileService;
import com.zincoid.nullbot.util.MsgParseUtil;
import com.zincoid.nullbot.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@CommandMapping({"ImageDel", "删除图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageDeleteCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        String directory = fileStorageProperties.getImagePath() + "/collect";
        ArrayMsg reply = event.getArrayMsg().getFirst();

        if (!params.isEmpty()) {
            deleteFile(bot, event, directory, params.getFirst());
            return;
        }
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MsgParseUtil
                    .extractImgMap(replyMsg.getRawMessage());
            if (imageMap.isEmpty())
                throw new NullBotMsgException("[删除图片] ❌未引用图片");
            for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                String originName = entry.getKey();  // QQ图片信息后缀全是jpg
                String name = originName.substring(0, originName.lastIndexOf("."));
                List<FilePO> realFiles = fileService.search(name, directory);
                if (realFiles.size() != 1)
                    throw new NullBotMsgException("[删除图片] ❌数据异常");
                deleteFile(bot, event, directory, realFiles.getFirst().getFileName());
            }
            return;
        }

        throw new NullBotMsgException("[删除图片] ❌无文件名或引用");
    }

    private void deleteFile(Bot bot, GroupMessageEvent event, String directory, String fileName) {
        if (!fileService.deleteFile(directory, fileName))
            throw new NullBotMsgException("[删除图片] ❌失败");
        bot.sendGroupMsg(event.getGroupId(), "[删除图片] ⚠️已删除\n- " +
                StringUtil.truncateFileName(fileName, 12), false);
        log.info("\t\t\t\t├─[ImageDelete] 图片已删除 - {}", fileName);
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
