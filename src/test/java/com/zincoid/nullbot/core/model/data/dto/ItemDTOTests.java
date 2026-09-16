package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.enums.data.Category;
import com.zincoid.nullbot.core.enums.data.Rarity;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemDTOTests {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private ItemDTO valid() {
        ItemDTO dto = new ItemDTO();
        dto.setName("徽章");
        dto.setRarity(Rarity.WHITE);
        dto.setCategory(Category.BREAD);
        dto.setPrice(50);
        dto.setWeight(1);
        dto.setDescription("测试物品");
        dto.setCommand("/badge");
        dto.setImagePath("/img/badge.png");
        dto.setAvailable(true);
        return dto;
    }

    @Test
    void validDtoPasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void blankNameRejected() {
        ItemDTO dto = valid();
        dto.setName("");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void nullRarityRejected() {
        ItemDTO dto = valid();
        dto.setRarity(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativePriceRejected() {
        ItemDTO dto = valid();
        dto.setPrice(-1);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void idOptional() {
        assertTrue(valid().getId() == null);
    }

    @Test
    void toPoMapsFields() {
        ItemDTO dto = valid();
        dto.setId(7);
        ItemPO po = dto.toPo();
        assertTrue(po.getId() == 7 && "徽章".equals(po.getName())
                && po.getPrice() == 50 && Boolean.TRUE.equals(po.getAvailable()));
    }

    @Test
    void toPoKeepsNullId() {
        assertNull(valid().toPo().getId());
    }
}
