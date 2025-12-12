package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.InventoryPO;
import org.bot.nullbot.dao.po.UserPO;

import java.util.List;

public interface UserService
{
    boolean decreaseDrawTimes(Long userId);

    List<InventoryPO> getInventories(Long userId);

    UserPO getUser(Long userId);
}
