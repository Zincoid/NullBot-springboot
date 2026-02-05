package org.bot.nullbot.service;

public interface SystemService
{
    void restart();

    void restartViaJar();

    void restartViaJar(String jarPath);

    String invoke(String beanName, String methodName, Object[] args) throws Exception;
}
