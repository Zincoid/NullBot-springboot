package com.zincoid.nullbot.core.service.base.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.converter.ItemConverter;
import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.model.data.dto.ItemDTO;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.ItemMapper;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.enums.data.Category;
import com.zincoid.nullbot.core.enums.data.Rarity;
import com.zincoid.nullbot.core.utils.DrawUtil;
import com.zincoid.nullbot.core.service.base.ItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, ItemPO> implements ItemService {

    @Override
    public void add(ItemDTO item) {
        item.setId(null);
        if (!save(ItemConverter.INSTANCE.toPO(item)))
            throw new CoreException("新增失败");
    }

    @Override
    public void update(ItemDTO item) {
        if (item.getId() == null)
            throw new CoreException("ID不能为空");
        if (!updateById(ItemConverter.INSTANCE.toPO(item)))
            throw new CoreException("更新失败");
    }

    @Override
    public void delete(Integer id) {
        if (!removeById(id))
            throw new CoreException("删除失败");
    }

    @Override
    public PageResult<ItemPO> page(ItemQuery query) {
        return PageResult.of(page(query.toPage(), null));
    }

    @Override
    public ItemPO getRandom() {
        Rarity rarity = DrawUtil.drawRarityByProbability();
        List<ItemPO> itemList = lambdaQuery()
                .ne(ItemPO::getCategory, Category.BREAD)
                .eq(ItemPO::getAvailable, true)
                .eq(ItemPO::getRarity, rarity)
                .list();
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    public ItemPO getRandomHighValue() {
        Rarity rarity = Rarity.GOLD;
        List<ItemPO> itemList = lambdaQuery()
                .ne(ItemPO::getCategory, Category.BREAD)
                .eq(ItemPO::getAvailable, true)
                .ge(ItemPO::getRarity, rarity)
                .list();
        return DrawUtil.drawItemByLogPrice(itemList);
    }

}
