package com.zincoid.nullbot.core.converter;

import com.zincoid.nullbot.core.model.data.dto.InventoryDTO;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InventoryConverter {

    InventoryConverter INSTANCE = Mappers.getMapper(InventoryConverter.class);

    InventoryPO toPO(InventoryDTO dto);
}
