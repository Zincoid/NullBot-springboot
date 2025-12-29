package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.mapper.SayingMapper;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl implements SayingService
{
    private final SayingMapper sayingMapper;

    // =================== BOT功能相关 ===================

    @Override
    @Transactional
    public int insert(Long userId, String userName, String text) {
        try {
            return sayingMapper.insert(userId, userName, text);
        } catch (Exception e) {
            return 0;
        }
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

    // =================== WEB功能相关 ===================

    @Override
    @Transactional
    public SayingPage getSayingByPage(Integer currentPage, Integer pageSize) {
        Page<SayingPO> page = new Page<>(currentPage, pageSize);
        Page<SayingPO> sayingPage = sayingMapper.selectPage(page, new LambdaQueryWrapper<SayingPO>().orderByDesc(SayingPO::getTime));
        return new SayingPage(sayingPage.getRecords(), sayingPage.getCurrent(), sayingPage.getPages(), sayingPage.getTotal(), sayingPage.getSize());
    }
}
