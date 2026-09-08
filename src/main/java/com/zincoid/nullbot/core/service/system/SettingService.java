package com.zincoid.nullbot.core.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.po.SettingPO;

import java.util.List;

public interface SettingService extends IService<SettingPO> {

    SettingPO get(Long groupId);

    boolean set(SettingPO setting);

    List<SettingPO> getAll();

    void setAll(List<SettingPO> settings);

    boolean removeByGroup(Long groupId);
}
