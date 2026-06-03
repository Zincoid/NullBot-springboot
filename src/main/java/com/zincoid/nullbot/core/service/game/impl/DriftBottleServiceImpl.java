package com.zincoid.nullbot.core.service.game.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.DriftBottlePO;
import com.zincoid.nullbot.core.mapper.DriftBottleMapper;
import com.zincoid.nullbot.core.service.game.DriftBottleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class DriftBottleServiceImpl implements DriftBottleService {

    private final DriftBottleMapper driftBottleMapper;

    @Override
    public boolean add(DriftBottlePO bottle) {
        try {
            return driftBottleMapper.insert(bottle) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean add(Long userId, String userName, String content, boolean isImage) {
        try {
            DriftBottlePO bottle = new DriftBottlePO();
            bottle.setUserId(userId);
            bottle.setUserName(userName);
            bottle.setContent(content);
            bottle.setIsImage(isImage);
            bottle.setTime(LocalDateTime.now());
            return driftBottleMapper.insert(bottle) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public DriftBottlePO pick() {
        long count = driftBottleMapper.selectCount(null);
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        DriftBottlePO bottle = driftBottleMapper.getOneByOffset(randomOffset);
        if (bottle != null) driftBottleMapper.deleteById(bottle.getId());
        return bottle;
    }
}
