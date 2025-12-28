package org.bot.nullbot.service;

public interface AccessService
{
    int getGroupAccess(Long groupId);

    int getUserAccess(Long userId);

    boolean setGroupAccess(long groupId, int newAccess);

    boolean setUserAccess(long userId, int newAccess);
}
