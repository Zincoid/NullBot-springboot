package com.zincoid.nullbot.core.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.data.dto.SettingDTO;
import com.zincoid.nullbot.core.model.data.po.SettingPO;

import java.util.List;

public interface SettingService extends IService<SettingPO> {

    SettingPO get(Long groupId);

    void set(SettingDTO setting);

    void set(SettingPO setting);

    boolean delete(Long groupId);

    List<SettingPO> getAll();

    void setAll(List<SettingPO> settings);
}
