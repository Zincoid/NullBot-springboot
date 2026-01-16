package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.TtsTemplatePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.mapper.TtsTemplateMapper;
import org.bot.nullbot.service.TtsTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TtsTemplateServiceImpl implements TtsTemplateService
{
    private final TtsTemplateMapper ttsTemplateMapper;

    @Override
    public boolean addTemplate(String name, String path, String text, Long userId, String userName) {
        TtsTemplatePO ttsTemplate = new TtsTemplatePO();
        ttsTemplate.setName(name);
        ttsTemplate.setPath(path);
        ttsTemplate.setText(text);
        ttsTemplate.setOwnerId(userId);
        ttsTemplate.setOwnerName(userName);
        try {
            return ttsTemplateMapper.insert(ttsTemplate) == 1;
        } catch (Exception e) {
            throw new IllegalArgumentException("模板名称冲突");
        }
    }

    @Override
    public boolean deleteTemplate(String templateName) {
        return ttsTemplateMapper.delete(new LambdaQueryWrapper<TtsTemplatePO>().eq(TtsTemplatePO::getName, templateName)) == 1;
    }

    @Override
    public TtsTemplatePO getTemplate(String templateName) {
        return ttsTemplateMapper.selectOne(new LambdaQueryWrapper<TtsTemplatePO>().eq(TtsTemplatePO::getName, templateName));
    }

    @Override
    public List<TtsTemplatePO> getTemplateList() {
        return ttsTemplateMapper.selectList(null);
    }
}
