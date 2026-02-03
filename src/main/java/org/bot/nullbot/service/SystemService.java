package org.bot.nullbot.service;

public interface SystemService
{
    void restart();

    String invoke(String beanName, String methodName, Object[] args) throws Exception;
}
