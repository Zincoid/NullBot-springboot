package com.zincoid.nullbot.core.service.tts.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.po.TtsTemplatePO;
import com.zincoid.nullbot.core.mapper.TtsTemplateMapper;
import com.zincoid.nullbot.core.service.tts.TtsTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsTemplateServiceImpl extends ServiceImpl<TtsTemplateMapper, TtsTemplatePO> implements TtsTemplateService {

    @Override
    public boolean add(String name, String path, String text, Long userId, String userName) {
        TtsTemplatePO ttsTemplate = new TtsTemplatePO();
        ttsTemplate.setName(name);
        ttsTemplate.setPath(path);
        ttsTemplate.setText(text);
        ttsTemplate.setOwnerId(userId);
        ttsTemplate.setOwnerName(userName);
        try {
            return save(ttsTemplate);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(String name) {
        return lambdaUpdate().eq(TtsTemplatePO::getName, name).remove();
    }

    @Override
    public TtsTemplatePO get(String name) {
        return lambdaQuery().eq(TtsTemplatePO::getName, name).one();
    }

    @Override
    @Transactional
    public void increaseUsed(Integer id) {
        TtsTemplatePO template = getById(id);
        if (template == null) return;
        template.setUsed(template.getUsed() + 1);
        updateById(template);
    }
}
