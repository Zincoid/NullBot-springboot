package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.model.data.dto.UserDTO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    UserPO toPO(UserDTO dto);
}
