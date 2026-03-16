package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.mapper.SayingMapper;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl implements SayingService
{
    private final SayingMapper sayingMapper;

    // =================== BOT功能相关 ===================

    @Override
    public int addSaying(Long userId, String userName, String text) {
        try {
            SayingPO saying = new SayingPO();
            saying.setUserId(userId);
            saying.setUserName(userName);
            saying.setText(text);
            saying.setTime(LocalDateTime.now());
            return sayingMapper.insert(saying);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        return sayingMapper.deleteById(id) == 1;
    }

    @Override
    public SayingPO getRand() {
        long count = sayingMapper.selectCount(null);
        if (count == 0) return null;
        long randomOffset = new Random().nextLong(0, count);
        return sayingMapper.getOneByOffset(randomOffset);
    }

    @Override
    public SayingPO getRandByUserId(Long userId) { return sayingMapper.getRandById(userId); }

    // =================== WEB功能相关 ===================

    @Override
    public List<SayingPO> getSayingList() { return sayingMapper.selectList(null); }

    @Override
    public SayingPage getSayingByPage(Integer currentPage, Integer pageSize) {
        Page<SayingPO> page = new Page<>(currentPage, pageSize);
        Page<SayingPO> sayingPage = sayingMapper.selectPage(page, new LambdaQueryWrapper<SayingPO>().orderByDesc(SayingPO::getTime));
        return new SayingPage(sayingPage.getRecords(), sayingPage.getCurrent(), sayingPage.getPages(), sayingPage.getTotal(), sayingPage.getSize());
    }

    @Override
    public void addSayings(List<SayingPO> sayings) { sayingMapper.insert(sayings); }
}
