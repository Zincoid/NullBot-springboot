package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.converter.GroupConverter;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupDTOTests {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private GroupDTO valid() {
        GroupDTO dto = new GroupDTO();
        dto.setId(123456L);
        dto.setName("测试群");
        dto.setAccess(1);
        return dto;
    }

    @Test
    void validDtoPasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void nullIdRejected() {
        GroupDTO dto = valid();
        dto.setId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void blankNameRejected() {
        GroupDTO dto = valid();
        dto.setName("  ");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativeAccessRejected() {
        GroupDTO dto = valid();
        dto.setAccess(-1);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void toPoMapsAllFields() {
        GroupPO po = GroupConverter.INSTANCE.toPO(valid());
        assertTrue(123456L == po.getId() && "测试群".equals(po.getName()) && 1 == po.getAccess());
    }
}
