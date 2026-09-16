package com.zincoid.nullbot.core.service.system.impl;

import com.zincoid.nullbot.core.exception.CoreException;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.module.system.Restarter;
import com.zincoid.nullbot.core.module.system.Invoker;
import com.zincoid.nullbot.core.module.control.FunctionManager;
import com.zincoid.nullbot.core.properties.ai.OpenAiProperties;
import com.zincoid.nullbot.core.model.data.vo.ModelVO;
import com.zincoid.nullbot.core.service.system.SystemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final Restarter restarter;
    private final Invoker invoker;
    private final FunctionManager functionManager;
    private final OpenAiProperties openAiProperties;

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
    public String invoke(String command) throws Exception {
        List<String> params = List.of(command.split("\\s+"));
        if (params.size() < 2)
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
    public Map<String, Boolean> getFuncFlags() {
        return functionManager.getStatus();
    }

    @Override
    public void setFuncFlag(String function, Boolean enabled) {
        if (enabled == null) {
            functionManager.switchEnabled(function);
            return;
        }
        functionManager.setEnabled(function, enabled);
    }

    @Override
    public ModelVO getModels() {
        ModelVO vo = new ModelVO();
        vo.setActive(openAiProperties.current().getName());
        vo.setProviders(openAiProperties.getProviders() == null ? List.of() :
                openAiProperties.getProviders().stream()
                        .map(p -> new ModelVO.ProviderVO(p.getName(), p.getModel()))
                        .toList());
        return vo;
    }

    @Override
    public void setModel(String provider) {
        if (openAiProperties.find(provider) == null)
            throw new CoreException("未知供应商: " + provider);
        openAiProperties.switchTo(provider);
    }
}
