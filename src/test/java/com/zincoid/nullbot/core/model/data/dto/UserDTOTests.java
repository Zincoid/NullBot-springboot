package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.model.data.po.UserPO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDTOTests {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private UserDTO valid() {
        UserDTO dto = new UserDTO();
        dto.setId(1024L);
        dto.setName("用户A");
        dto.setAccess(1);
        dto.setLevel(3);
        dto.setExperience(80);
        dto.setCash(100);
        dto.setCapacity(10);
        dto.setDrawTimes(1);
        return dto;
    }

    @Test
    void validDtoPasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void nullIdRejected() {
        UserDTO dto = valid();
        dto.setId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void blankNameRejected() {
        UserDTO dto = valid();
        dto.setName(" ");
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativeCashRejected() {
        UserDTO dto = valid();
        dto.setCash(-5);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void toPoMapsAllFields() {
        UserPO po = valid().toPo();
        assertTrue(1024L == po.getId() && "用户A".equals(po.getName())
                && po.getAccess() == 1 && po.getLevel() == 3
                && po.getExperience() == 80 && po.getCash() == 100
                && po.getCapacity() == 10 && po.getDrawTimes() == 1);
    }
}
