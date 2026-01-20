package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.control.SpringInvoker;
import org.bot.nullbot.service.SystemService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService
{
    private final SpringInvoker invoker;

    @Override
    public String invoke(String beanName, String methodName, Object[] args) throws Exception {
        Object result = invoker.invokeSpringMethod(beanName, methodName, args);
        return result != null ? result.toString() : "null";
    }
}
