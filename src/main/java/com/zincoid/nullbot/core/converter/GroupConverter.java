package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.model.data.dto.GroupDTO;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GroupConverter {

    GroupConverter INSTANCE = Mappers.getMapper(GroupConverter.class);

    GroupPO toPO(GroupDTO dto);
}
