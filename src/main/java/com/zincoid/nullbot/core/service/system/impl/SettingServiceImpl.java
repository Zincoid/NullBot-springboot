package com.zincoid.nullbot.core.service.system.impl;

import jakarta.annotation.PostConstruct;
import com.zincoid.nullbot.core.exception.CoreException;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.mapper.SettingMapper;
import com.zincoid.nullbot.core.service.system.SettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, SettingPO> implements SettingService {

    private final Map<Long, SettingPO> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
        list().forEach(setting -> cache.put(setting.getGroupId(), setting));
        log.info("▽ [SettingService] 群组数据库配置已载入 - Configs: {} ", cache.size());
    }

    @Override
    public SettingPO get(Long groupId) {
        return cache.computeIfAbsent(groupId, k -> {
            SettingPO setting = lambdaQuery().eq(SettingPO::getGroupId, groupId).one();
            if (setting == null) {
                setting = new SettingPO(groupId);
                save(setting);
            }
            return setting;
        });
    }

    @Override
    public void set(SettingPO setting) {
        SettingPO existing = lambdaQuery().eq(SettingPO::getGroupId, setting.getGroupId()).one();
        boolean updated;
        if (existing != null) {
            setting.setId(existing.getId());
            updated = updateById(setting);
        } else updated = save(setting);
        if (!updated)
            throw new CoreException("更新失败");
        cache.put(setting.getGroupId(), setting);
    }

    @Override
    public List<SettingPO> getAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void setAll(List<SettingPO> settings) {
        settings.forEach(this::set);
    }

    @Override
    public boolean removeByGroup(Long groupId) {
        cache.remove(groupId);
        return lambdaUpdate().eq(SettingPO::getGroupId, groupId).remove();
    }
}
