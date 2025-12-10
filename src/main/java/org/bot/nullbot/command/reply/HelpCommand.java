package org.bot.nullbot.command.reply;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.StaticResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

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
            // String help = """
            //         ◉ [AI功能]
            //         Chat 或 @Null 或 戳一戳  与AI对话
            //         ChatReset  清空历史记忆
            //         ChatHistory   获取聊天历史
            //         ChatMode  切换聊天模式
            //
            //         ◉ [语录功能]
            //         SayingSave  保存语录
            //         SayingDelete  删除语录
            //         RandomSaying 或 say  随机语录
            //
            //         ◉ [图片功能]
            //         ImageSave  保存引用图片
            //         ImageGet  获取保存图片
            //         ImageDelete  删除保存图片(可引用删除)
            //         ImageList  获取保存图片列表
            //         RandomImage 或 img  发送随机保存图片
            //         ImageFolderStructure  获取图片文件夹树状结构
            //
            //         ◉ [视频功能]
            //         VideoSave  保存引用视频
            //         VideoGet  获取保存视频
            //         VideoDelete  删除保存视频(可引用删除)
            //         VideoList  获取保存视频列表
            //         RandomVideo 或 video  发送随机保存视频
            //
            //         ◉ [娱乐功能]
            //         Wife  今日老婆(群友/二次元)
            //
            //         ◉ [管理功能]
            //         UserBan  禁言群内用户
            //         FunctionCheck  检查功能启用状态
            //         FunctionControl  修改功能启用状态
            //         AccessSet  设置用户限权
            //
            //         ◉ [帮助功能]
            //         Help 或 help 或 "帮助" 获取帮助信息
            //         指令后加 -help 或 -h 获取详情
            //
            //         注:
            //         1. 图片保存路径为{配置路径}/collect 图片自动收集路径为{配置路径}/monitor
            //         2. 选老婆功能的二次元可选人物来源可通过ImageFolderStructure查看acg下子目录名获得
            //         3. 使用AI Monitor模式及Recall Detect功能 需通过FunctionControl启用MessageCollect
            //         4. 中文命令 在对应指令详情中获得
            //         5. 当前指令前缀为\s""" + commandPrefix;

            // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "======= HELP =======\n" + help, false);

            try {
                String helpBase64 = StaticResourceUtil.loadImageAsBase64("help.png");
                String response = MsgUtils.builder().img("base64://" + helpBase64).build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                logger.info("\t\t\t\t├─[Help] 已获取帮助");
            } catch (IOException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Help] ❌帮助资源缺失", false);
                logger.info("\t\t\t\t├─[Help] 帮助资源缺失");
            }
        }else
            logger.info("\t\t\t\t├─[Help] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() { return "何意味?"; }
}
