package com.zincoid.nullbot.core.service.system;

import com.zincoid.nullbot.core.model.data.dto.AdminDTO;
import com.zincoid.nullbot.core.model.data.dto.LoginDTO;
import com.zincoid.nullbot.core.model.data.dto.PasswordDTO;
import com.zincoid.nullbot.core.model.data.dto.RegistDTO;

import java.util.Map;

public interface AuthService {

    void regist(RegistDTO regist);

    String login(LoginDTO login);

    String guest();

    void update(AdminDTO admin);

    void delete(Long id);

    void changePassword(Long id, PasswordDTO password);

    Map<String, Object> info(Integer type, Long id);
}
