package org.bot.nullbot.converter;

import org.bot.nullbot.entity.dto.AdminUpdateDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AdminConverter {

    AdminConverter INSTANCE = Mappers.getMapper(AdminConverter.class);

    public AdminPO toPO(AdminUpdateDTO dto);

    public AdminPO toPO(UserPO user);
}
