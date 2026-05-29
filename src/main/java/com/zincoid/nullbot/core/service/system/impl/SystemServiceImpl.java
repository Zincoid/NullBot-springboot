package com.zincoid.nullbot.core.service.system.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.component.tool.Restarter;
import com.zincoid.nullbot.core.component.tool.Invoker;
import com.zincoid.nullbot.core.service.system.SystemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final Restarter restarter;
    private final Invoker invoker;

    @Override
    public String invoke(String command) throws Exception {
        List<String> params = List.of(command.split(" "));
        if(params.size() < 2)
            throw new IllegalArgumentException("Not enough args...");
        String beanName = params.get(0);
        String methodName = params.get(1);
        Object[] args = new Object[0];
        if (params.size() > 2) args = params.subList(2, params.size()).toArray();
        return invoke(beanName, methodName, args);
    }

    @Override
    public String invoke(String beanName, String methodName, Object[] args) throws Exception {
        Object result = invoker.invokeSpringMethod(beanName, methodName, args);
        return result != null ? result.toString() : "null";
    }

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
}
