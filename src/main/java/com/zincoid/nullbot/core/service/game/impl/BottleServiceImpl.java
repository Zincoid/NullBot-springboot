package com.zincoid.nullbot.core.service.game.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.BottlePO;
import com.zincoid.nullbot.core.mapper.BottleMapper;
import com.zincoid.nullbot.core.service.game.BottleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BottleServiceImpl implements BottleService {

    private final BottleMapper bottleMapper;

    @Override
    public boolean add(BottlePO bottle) {
        try {
            return bottleMapper.insert(bottle) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean add(Long userId, String userName, String content, boolean isImage) {
        try {
            BottlePO bottle = new BottlePO();
            bottle.setUserId(userId);
            bottle.setUserName(userName);
            bottle.setContent(content);
            bottle.setIsImage(isImage);
            bottle.setTime(LocalDateTime.now());
            return bottleMapper.insert(bottle) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public BottlePO pick() {
        long count = bottleMapper.selectCount(null);
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        BottlePO bottle = bottleMapper.getOneByOffset(randomOffset);
        if (bottle != null) bottleMapper.deleteById(bottle.getId());
        return bottle;
    }
}
