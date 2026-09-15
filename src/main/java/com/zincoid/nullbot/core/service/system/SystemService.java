package com.zincoid.nullbot.core.service.system;

import com.zincoid.nullbot.core.model.data.vo.ModelVO;

import java.util.Map;

public interface SystemService {

    void restart();

    void restartViaJar();

    void restartViaJar(String jarPath);

    String invoke(String command) throws Exception;

    String invoke(String beanName, String methodName, Object[] args) throws Exception;

    Map<String, Boolean> getFuncFlags();

    void setFuncFlag(String function, Boolean enabled);

    ModelVO getModels();

    void setModel(String provider);
}
