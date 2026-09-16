package com.zincoid.nullbot.core.service.system;

import com.zincoid.nullbot.core.model.data.vo.StatsVO;

public interface StatsService {

    void increaseDaily();

    void increase(Long groupId, Long userId, String command);

    StatsVO getStats();

    Long getUsage(Long userId);
}
