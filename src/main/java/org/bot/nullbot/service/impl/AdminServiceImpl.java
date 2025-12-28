package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.dto.LoginDTO;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.mapper.AdminMapper;
import org.bot.nullbot.service.AdminService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService
{
    private final AdminMapper adminMapper;

    @Override
    public boolean login(LoginDTO loginDTO) {
        AdminPO admin = adminMapper.selectById(loginDTO.getId());
        return admin != null && admin.getPassword().equals(loginDTO.getPassword());
    }

    @Override
    public AdminPO info(Long id) {
        return adminMapper.selectById(id);
    }
}
