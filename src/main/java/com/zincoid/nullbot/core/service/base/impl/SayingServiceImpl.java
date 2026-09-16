package com.zincoid.nullbot.core.service.base.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.model.data.query.SayingQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.SayingMapper;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.service.base.SayingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SayingServiceImpl extends ServiceImpl<SayingMapper, SayingPO> implements SayingService {

    @Override
    public boolean add(Long userId, String userName, String text) {
        SayingPO saying = new SayingPO();
        saying.setUserId(userId);
        saying.setUserName(userName);
        saying.setText(text);
        saying.setTime(LocalDateTime.now());
        return save(saying);
    }

    @Override
    public void delete(Integer id) {
        if (!removeById(id))
            throw new CoreException("语录不存在");
    }

    public PageResult<SayingPO> page(SayingQuery query) {
        return PageResult.of(page(query.toPage(), null));
    }

    @Override
    public SayingPO random() {
        long count = count();
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        return baseMapper.getOneByOffset(randomOffset);
    }

    @Override
    public SayingPO random(Long userId) {
        long count = lambdaQuery().eq(SayingPO::getUserId, userId).count();
        if (count == 0) return null;
        long randomOffset = ThreadLocalRandom.current().nextLong(0, count);
        return baseMapper.getOneByOffsetAndUserId(userId, randomOffset);
    }
}
