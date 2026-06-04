package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.ItemMapper;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.enums.Category;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.util.DrawUtil;
import com.zincoid.nullbot.core.service.basic.ItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, ItemPO> implements ItemService {

    @Override
    public PageResult<ItemPO> getPage(ItemQuery query) {
        return PageResult.of(page(query.toPage(), null));
    }

    @Override
    public boolean exist(Integer id) {
        return lambdaQuery().eq(ItemPO::getId, id).exists();
    }

    @Override
    public boolean isUsable(Integer id) {
        return getById(id).getCommand() != null;
    }

    @Override
    public String getCommand(Integer id) {
        return  getById(id).getCommand();
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
