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

import java.util.List;

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
        List<String> methodArgsStr = args.getParams();
        Object[] methodArgs = new Object[0];
        if (methodArgsStr.size() > 2)
            methodArgs = methodArgsStr.subList(2, methodArgsStr.size()).toArray();
        try {
            String result = systemService.invoke(beanName, methodName, methodArgs);
            bot.sendGroupMsg(event.getGroupId(), """
                    [Spring] ✅方法调用成功
                    The method returned:
                    %s""".formatted(result), false
            );
            log.info("☑ [Invoke] 调用结果 -> {}", result);
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