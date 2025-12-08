package org.bot.qqbot.command.reply;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.qqbot.annotation.CommandMapping;
import org.bot.qqbot.command.Command;
import org.bot.qqbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"Help", "help"})
@Component
public class HelpCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String help = """
                    ◉ [AI功能]
                    Chat 或 @Null 或 戳一戳  与AI对话
                    ResetHistory  清空历史记忆
                    ChatHistory   获取聊天历史
                    ChatMode  切换聊天模式
                    
                    ◉ [图片功能]
                    ImageSave  保存引用的图片至本地
                    ImageGet  获取保存的图片
                    ImageDelete  删除保存的图片
                    ImageList  获取保存图片的列表
                    RandomImage 或 img  发送保存的随机图片

                    ◉ [管理功能]
                    UserBan  禁言群内用户
                    FunctionControl  修改部分功能启用状态
                    AccessSet  设置用户限权
                    
                    ◉ [帮助功能]
                    Help  获取帮助信息
                    指令后加 -help或-h 获取详情
                    
                    指令前缀为 /""";

            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "======= HELP =======\n" + help, false);
            logger.info("\t\t\t\t├─[Help] 已打印帮助");
        }else
            logger.info("\t\t\t\t├─[Help] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() { return "何意味?"; }
}
