package com.zincoid.nullbot.core.service.system;

public interface SystemService {

    String invoke(String command) throws Exception;

    String invoke(String beanName, String methodName, Object[] args) throws Exception;

    void restart();

    void restartViaJar();

    void restartViaJar(String jarPath);
}
