package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.model.data.dto.ItemDTO;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemConverter {

    ItemConverter INSTANCE = Mappers.getMapper(ItemConverter.class);

    ItemPO toPO(ItemDTO dto);
}
