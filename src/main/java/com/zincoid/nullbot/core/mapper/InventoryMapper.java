package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;

import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryPO> {

    @Select("SELECT i.id, i.owner_id, i.item_id, i.amount, " +
           "it.name AS item_name, it.category, it.rarity, it.price " +
           "FROM inventory i " +
           "JOIN item it ON i.item_id = it.id " +
           "WHERE i.owner_id = #{userId} " +
           "ORDER BY it.rarity DESC, it.price DESC, i.id")
    Page<InventoryVO> selectVOPage(Page<InventoryVO> page, Long userId);

    @Select("SELECT i.id, i.owner_id, i.item_id, i.amount, " +
           "it.name AS item_name, it.category, it.rarity, it.price " +
           "FROM inventory i " +
           "JOIN item it ON i.item_id = it.id " +
           "WHERE i.owner_id = #{userId} " +
           "ORDER BY it.rarity DESC, it.price DESC, i.id")
    List<InventoryVO> selectVOList(Long userId);

    @Select("SELECT COALESCE(SUM(amount), 0) FROM inventory WHERE owner_id = #{userId}")
    Integer getTotalAmountByUserId(Long userId);
}
