package com.zincoid.nullbot.service;

import com.zincoid.nullbot.entity.po.TtsTemplatePO;

import java.util.List;

public interface TtsTemplateService {

    boolean add(String name, String path, String text, Long userId, String userName);

    boolean deleteByName(String name);

    TtsTemplatePO getByName(String name);

    List<TtsTemplatePO> getList();

    void increaseUsed(Integer id);
}
