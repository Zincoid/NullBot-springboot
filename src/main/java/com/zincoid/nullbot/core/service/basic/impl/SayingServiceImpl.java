package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.DataPage;
import com.zincoid.nullbot.core.mapper.SayingMapper;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.service.basic.SayingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl implements SayingService {

    private final SayingMapper sayingMapper;

    // =================== BOT功能相关 ===================

    @Override
    public boolean add(Long userId, String userName, String text) {
        SayingPO saying = new SayingPO();
        saying.setUserId(userId);
        saying.setUserName(userName);
        saying.setText(text);
        saying.setTime(LocalDateTime.now());
        return sayingMapper.insert(saying) == 1;
    }

    @Override
    public boolean deleteById(Integer id) {
        return sayingMapper.deleteById(id) == 1;
    }

    @Override
    public SayingPO getRand() {
        long count = sayingMapper.selectCount(null);
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        return sayingMapper.getOneByOffset(randomOffset);
    }

    @Override
    public SayingPO getRandByUserId(Long userId) {
        long count = sayingMapper.selectCount(new LambdaQueryWrapper<SayingPO>().eq(SayingPO::getUserId, userId));
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        return sayingMapper.getOneByOffsetAndUserId(userId, randomOffset);
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<SayingPO> getList() {
        return sayingMapper.selectList(null);
    }

    @Override
    public DataPage<SayingPO> getPage(Integer current, Integer size) {
        Page<SayingPO> page = Page.of(current, size);
        Page<SayingPO> sayingPage = sayingMapper.selectPage(page, new LambdaQueryWrapper<SayingPO>().orderByDesc(SayingPO::getTime));
        return new DataPage<>(sayingPage.getRecords(), sayingPage.getCurrent(), sayingPage.getPages(), sayingPage.getTotal(), sayingPage.getSize());
    }

    @Override
    public void adds(List<SayingPO> sayings) {
        sayingMapper.insert(sayings);
    }
}
