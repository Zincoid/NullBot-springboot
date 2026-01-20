package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.SpringInvoker;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Invoke", "调用"})
@Component
@Slf4j
@RequiredArgsConstructor
public class InvokeCommand implements Command
{
    private final SpringInvoker invoker;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.size() < 2) throw new NullBotMsgException("[Spring] ❌未指定Bean和Method");

            String beanName = params.get(0);
            String methodName = params.get(1);
            Object[] args = new Object[0];
            if (params.size() > 2) args = params.subList(2, params.size()).toArray();

            try {
                Object object = invoker.invokeSpringMethod(beanName, methodName, args);
                String result = object != null ? object.toString() : "null";
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Spring] ✅方法调用成功\nThe method returned:\n" + result, false);
                log.info("\t\t\t\t├─[Invoke] 调用结果 -> {}", result);
            } catch (Exception e) {
                throw new NullBotMsgException("[Spring] ⚠️方法调用失败\n" + e.getMessage());
            }
        } else
            throw new NullBotLogException("[Spring] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Invoke 命令
                功能: 反射调用Spring方法
                限权: %d 级
                格式: Invoke [Bean名] [方法名] [参数...]
                别名: 调用""", getAccess()
        );
    }
}