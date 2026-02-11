package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.tool.Restarter;
import org.bot.nullbot.component.tool.Invoker;
import org.bot.nullbot.service.SystemService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService
{
    private final Restarter restarter;
    private final Invoker invoker;

    @Override
    public void restart() {
        restarter.restart();
    }

    @Override
    public void restartViaJar() {
        restarter.restartViaJar();
    }

    @Override
    public void restartViaJar(String jarPath) {
        restarter.restartViaJar(jarPath);
    }

    @Override
    public String invoke(String beanName, String methodName, Object[] args) throws Exception {
        Object result = invoker.invokeSpringMethod(beanName, methodName, args);
        return result != null ? result.toString() : "null";
    }
}
