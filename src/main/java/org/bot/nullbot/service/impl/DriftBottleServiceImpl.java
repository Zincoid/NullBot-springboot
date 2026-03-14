package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.DriftBottlePO;
import org.bot.nullbot.mapper.DriftBottleMapper;
import org.bot.nullbot.service.DriftBottleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DriftBottleServiceImpl implements DriftBottleService
{
    private final DriftBottleMapper driftBottleMapper;

    @Override
    public int throwBottle(DriftBottlePO bottle) {
        try {
            return driftBottleMapper.insert(bottle);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int throwBottle(Long userId, String userName, String text) {
        try {
            DriftBottlePO bottle = new DriftBottlePO();
            bottle.setUserId(userId);
            bottle.setUserName(userName);
            bottle.setText(text);
            bottle.setTime(LocalDateTime.now());
            return driftBottleMapper.insert(bottle);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    @Transactional
    public DriftBottlePO pickUpRand() {
        DriftBottlePO bottle = driftBottleMapper.getRand();
        if (bottle != null) driftBottleMapper.deleteById(bottle.getId());
        return bottle;
    }
}
