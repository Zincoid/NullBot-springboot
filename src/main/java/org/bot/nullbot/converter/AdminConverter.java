package org.bot.nullbot.converter;

import org.bot.nullbot.entity.dto.AdminUpdateDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.po.UserPO;
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
