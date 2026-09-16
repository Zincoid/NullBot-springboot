package com.zincoid.nullbot.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.mapper.AdminMapper;
import com.zincoid.nullbot.core.model.data.po.AdminPO;
import com.zincoid.nullbot.core.service.system.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, AdminPO> implements AdminService {
}
