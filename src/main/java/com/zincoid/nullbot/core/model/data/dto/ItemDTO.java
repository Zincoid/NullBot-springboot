package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.enums.data.Category;
import com.zincoid.nullbot.core.enums.data.Rarity;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDTO {

    private Integer id;

    @NotBlank(message = "物品名不能为空")
    private String name;

    @NotNull(message = "稀有度不能为空")
    private Rarity rarity;

    @NotNull(message = "类别不能为空")
    private Category category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负数")
    private Integer price;

    @DecimalMin(value = "0", message = "重量不能为负数")
    private Integer weight;

    private String description;
    private String command;
    private String imagePath;
    private Boolean available;

    public ItemPO toPo() {
        ItemPO po = new ItemPO();
        po.setId(id);
        po.setName(name);
        po.setRarity(rarity);
        po.setCategory(category);
        po.setPrice(price);
        po.setWeight(weight);
        po.setDescription(description);
        po.setCommand(command);
        po.setImagePath(imagePath);
        po.setAvailable(available);
        return po;
    }
}
