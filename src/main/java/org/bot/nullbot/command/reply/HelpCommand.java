package org.bot.nullbot.command.reply;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@CommandMapping({"Help", "help", "帮助"})
@Component
public class HelpCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    @Value("${nullbot.command.prefix}")
    private String commandPrefix;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String help = """
                    ◉ [AI功能]
                    Chat 或 @Null 或 戳一戳  与AI对话
                    ChatReset  清空历史记忆
                    ChatHistory   获取聊天历史
                    ChatMode  切换聊天模式
                    
                    ◉ [语录功能]
                    SayingSave  保存语录
                    SayingDelete  删除语录
                    RandomSaying 或 say  随机语录
                    
                    ◉ [图片功能]
                    ImageSave  保存引用图片
                    ImageGet  获取保存图片
                    ImageDelete  删除保存图片(可引用删除)
                    ImageList  获取保存图片列表
                    RandomImage 或 img  发送随机保存图片
                    
                    ◉ [老婆功能]
                    Wife  今日群友老婆

                    ◉ [管理功能]
                    UserBan  禁言群内用户
                    FunctionControl  修改功能启用状态
                    AccessSet  设置用户限权
                    
                    ◉ [帮助功能]
                    Help 或 help 或 "帮助" 获取帮助信息
                    指令后加 -help 或 -h 获取详情
                    
                    中文命令 在对应指令详情中获得
                    当前指令前缀为\s""" + commandPrefix;

            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "======= HELP =======\n" + help, false);
            logger.info("\t\t\t\t├─[Help] 已打印帮助");
        }else
            logger.info("\t\t\t\t├─[Help] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() { return "何意味?"; }
}
