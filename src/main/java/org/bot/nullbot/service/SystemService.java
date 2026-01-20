package org.bot.nullbot.service;

public interface SystemService
{
    String invoke(String beanName, String methodName, Object[] args) throws Exception;
}
