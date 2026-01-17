package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.TtsTemplatePO;

import java.util.List;

public interface TtsTemplateService
{
    boolean addTemplate(String name, String path, String text, Long userId, String userName);

    boolean deleteTemplate(String templateName);

    TtsTemplatePO getTemplate(String templateName);

    List<TtsTemplatePO> getTemplateList();

    void increaseUsed(Integer id);
}
