package com.zincoid.nullbot.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.dto.page.DataPage;
import com.zincoid.nullbot.core.mapper.SayingMapper;
import com.zincoid.nullbot.core.model.po.SayingPO;
import com.zincoid.nullbot.core.service.SayingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl implements SayingService {

    private final SayingMapper sayingMapper;

    // =================== BOT功能相关 ===================

    @Override
    public int add(Long userId, String userName, String text) {
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
    public List<SayingPO> getList() { return sayingMapper.selectList(null); }

    @Override
    public DataPage<SayingPO> getPage(Integer current, Integer size) {
        Page<SayingPO> page = new Page<>(current, size);
        Page<SayingPO> sayingPage = sayingMapper.selectPage(page, new LambdaQueryWrapper<SayingPO>().orderByDesc(SayingPO::getTime));
        return new DataPage<>(sayingPage.getRecords(), sayingPage.getCurrent(), sayingPage.getPages(), sayingPage.getTotal(), sayingPage.getSize());
    }

    @Override
    public void adds(List<SayingPO> sayings) { sayingMapper.insert(sayings); }
}
