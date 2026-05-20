package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.SystemService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Invoke", "调用"})
@Component
@Slf4j
@RequiredArgsConstructor
public class InvokeCommand implements Command {

    private final SystemService systemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.size() < 2)
            throw new NullBotMsgException("[Spring] ❌未指定Bean和Method");

        String beanName = params.get(0);
        String methodName = params.get(1);
        Object[] args = new Object[0];

        if (params.size() > 2)
            args = params.subList(2, params.size()).toArray();

        try {
            String result = systemService.invoke(beanName, methodName, args);
            bot.sendGroupMsg(event.getGroupId(), """
                    [Spring] ✅方法调用成功
                    The method returned:
                    %s""".formatted(result), false
            );
            log.info("\t\t\t\t├─[Invoke] 调用结果 -> {}", result);
        } catch (Exception e) {
            throw new NullBotMsgException("[Spring] ⚠️方法调用失败\n" + e.getMessage());
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