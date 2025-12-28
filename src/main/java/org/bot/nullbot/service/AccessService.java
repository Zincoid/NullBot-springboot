package org.bot.nullbot.service;

public interface AccessService
{
    boolean existGroup(long targetId);

    boolean existUser(long targetId);

    int getGroupAccess(Long groupId);

    int getUserAccess(Long userId);

    void setGroupAccess(long groupId, int newAccess);

    void setUserAccess(long userId, int newAccess);
}
