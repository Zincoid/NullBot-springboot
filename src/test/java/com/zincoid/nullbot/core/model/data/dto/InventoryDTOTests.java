package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.converter.InventoryConverter;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryDTOTests {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private InventoryDTO valid() {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(9);
        dto.setOwnerId(1024L);
        dto.setItemId(3);
        dto.setAmount(2);
        return dto;
    }

    @Test
    void validDtoPasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void nullOwnerIdRejected() {
        InventoryDTO dto = valid();
        dto.setOwnerId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void nullItemIdRejected() {
        InventoryDTO dto = valid();
        dto.setItemId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativeAmountRejected() {
        InventoryDTO dto = valid();
        dto.setAmount(-1);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void toPoMapsAllFields() {
        InventoryPO po = InventoryConverter.INSTANCE.toPO(valid());
        assertTrue(po.getId() == 9 && po.getOwnerId() == 1024L
                && po.getItemId() == 3 && po.getAmount() == 2);
    }
}
