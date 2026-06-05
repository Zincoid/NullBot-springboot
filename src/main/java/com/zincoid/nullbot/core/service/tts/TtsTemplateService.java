package com.zincoid.nullbot.core.service.tts;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.po.TtsTemplatePO;

public interface TtsTemplateService extends IService<TtsTemplatePO> {

    boolean add(String name, String path, String text, Long userId, String userName);

    boolean delete(String name);

    TtsTemplatePO get(String name);

    void increaseUsed(Integer id);
}
