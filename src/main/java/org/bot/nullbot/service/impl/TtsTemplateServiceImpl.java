package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.po.TtsTemplatePO;
import org.bot.nullbot.mapper.TtsTemplateMapper;
import org.bot.nullbot.service.TtsTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsTemplateServiceImpl implements TtsTemplateService {

    private final TtsTemplateMapper ttsTemplateMapper;

    @Override
    public boolean add(String name, String path, String text, Long userId, String userName) {
        TtsTemplatePO ttsTemplate = new TtsTemplatePO();
        ttsTemplate.setName(name);
        ttsTemplate.setPath(path);
        ttsTemplate.setText(text);
        ttsTemplate.setOwnerId(userId);
        ttsTemplate.setOwnerName(userName);
        try {
            return ttsTemplateMapper.insert(ttsTemplate) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteByName(String name) {
        return ttsTemplateMapper.delete(new LambdaQueryWrapper<TtsTemplatePO>().eq(TtsTemplatePO::getName, name)) == 1;
    }

    @Override
    public TtsTemplatePO getByName(String name) {
        return ttsTemplateMapper.selectOne(new LambdaQueryWrapper<TtsTemplatePO>().eq(TtsTemplatePO::getName, name));
    }

    @Override
    public List<TtsTemplatePO> getAll() {
        return ttsTemplateMapper.selectList(null);
    }

    @Override
    @Transactional
    public void increaseUsed(Integer id) {
        TtsTemplatePO template = ttsTemplateMapper.selectById(id);
        if (template == null) return;
        template.setUsed(template.getUsed() + 1);
        ttsTemplateMapper.updateById(template);
    }
}
