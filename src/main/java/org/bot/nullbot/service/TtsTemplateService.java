package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.TtsTemplatePO;

import java.util.List;

public interface TtsTemplateService {

    boolean add(String name, String path, String text, Long userId, String userName);

    boolean delete(String name);

    TtsTemplatePO get(String name);

    List<TtsTemplatePO> getAll();

    void increaseUsed(Integer id);
}
