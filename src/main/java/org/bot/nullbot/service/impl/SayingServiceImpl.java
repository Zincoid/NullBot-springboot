package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dao.mapper.SayingMapper;
import org.bot.nullbot.dao.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl implements SayingService
{
    private final SayingMapper sayingMapper;

    @Override
    @Transactional
    public int insert(Long userId, String userName, String text) {
        return sayingMapper.insert(userId, userName, text);
    }

    @Override
    @Transactional
    public boolean deleteById(Integer id) {
        return sayingMapper.deleteById(id);
    }

    @Override
    @Transactional
    public List<SayingPO> getList() {
        return sayingMapper.getList();
    }

    @Override
    @Transactional
    public SayingPO getRand() {
        return sayingMapper.getRand();
    }
}
