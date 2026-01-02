package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

@CommandMapping({"1e7bd161-0273-4fd0-ae2e-907f25fd8bf3"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class BanChatCommand implements Command
{
    private final UserService userService;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        Long groupId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
        }else if(event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent){
            groupId = pokeNoticeEvent.getGroupId();
        }else{
            log.info("\t\t\t\t├─[Meme] 未设计 非群消息/戳一戳事件响应方式");
            return;
        }

        if(event.getCommandParameters().size() < 2) {
            bot.sendGroupMsg(groupId, "[封禁对话] ❌参数不足", false);
            log.info("\t\t\t\t├─[BanChat] 参数不足");
            return;
        }

        try {
            long userId = Long.parseLong(event.getCommandParameters().get(0));
            int time = Integer.parseInt(event.getCommandParameters().get(1));
            if(!userService.existUser(userId)) {
                bot.sendGroupMsg(groupId, "[封禁对话] ❌用户不存在", false);
                log.info("\t\t\t\t├─[BanChat] 用户不存在");
            }
            chatStorage.banUser(userId, time);
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            bot.sendGroupMsg(groupId, "[封禁对话] ✅已设置！\n" + userName + "(" + userId + ") -> " + time + " Min", false);
            log.info("\t\t\t\t├─[BanChat] 已封禁对话 - {} -> {} min", userId, time);
        } catch (NumberFormatException e) {
            bot.sendGroupMsg(groupId, "[封禁对话] ❌参数格式错误", false);
            log.info("\t\t\t\t├─[BanChat] 参数格式错误");
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ 1e7bd161-0273-4fd0-ae2e-907f25fd8bf3 命令
                功能: 临时封禁某人与你进行对话的功能(时长为0则解除封禁)
                限权: %d
                格式: 1e7bd161-0273-4fd0-ae2e-907f25fd8bf3 [QQ号] [时长(分钟)]
                示例: 1e7bd161-0273-4fd0-ae2e-907f25fd8bf3 1826403984 1
                注意: 当你对某人感到厌烦时可以调用此命令，这样他就不能在你设置的时限内与你对话了！
                不要太小肚鸡肠，别人只是揉你一两下的话不要立马封禁他们，而且就算封禁也最好只禁1分钟！""", getAccess()
        );
    }
}
