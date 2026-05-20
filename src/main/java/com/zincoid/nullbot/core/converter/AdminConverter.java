package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.entity.dto.AdminUpdateDTO;
import com.zincoid.nullbot.core.entity.po.AdminPO;
import com.zincoid.nullbot.core.entity.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminConverter {

    AdminConverter INSTANCE = Mappers.getMapper(AdminConverter.class);

    @Mapping(target = "password", ignore = true)
    public AdminPO toPO(AdminUpdateDTO dto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(source = "name", target = "username")
    public AdminPO toPO(UserPO user);
}
