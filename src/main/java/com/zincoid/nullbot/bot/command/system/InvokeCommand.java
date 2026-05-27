package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.SystemService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Invoke", "调用"})
@Component
@RequiredArgsConstructor
public class InvokeCommand implements Command {

    private final SystemService systemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        String beanName = args.nextString();
        String methodName = args.nextString();
        Object[] methodArgs = args.hasNext()
                ? args.getParams().subList(2, args.size()).toArray()
                : new Object[0];
        try {
            String result = systemService.invoke(beanName, methodName, methodArgs);
            bot.sendGroupMsg(event.getGroupId(), """
                    ✅方法已反射调用
                    - Method: %s.%s(..)
                    - Return: %s""".formatted(beanName, methodName, result), false
            );
            log.info("☑ [Invoke] 方法已反射调用 - {}.{}(..) -> {}", beanName, methodName, result);
        } catch (Exception e) {
            throw new BotWarnException("方法调用失败: " + e.getMessage());
        }
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