package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.model.data.dto.SettingDTO;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SettingConverter {

    SettingConverter INSTANCE = Mappers.getMapper(SettingConverter.class);

    @Mapping(target = "id", ignore = true)
    SettingPO toPO(SettingDTO dto);
}
