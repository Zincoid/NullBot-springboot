package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.zincoid.nullbot.bot.command.Cmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.service.basic.GroupService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class RegisterHandler implements Handler {

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {
        EventScope scope = event.getEventScope();

        if (scope == EventScope.UNKNOWN) {
            log.info("├─[RegisterHandler] 未知事件不可注册");
            chain.doHandle(bot, event, cmd);
            return;
        }
        if (scope == EventScope.GROUP) {
            Long groupId = event.getGroupId();
            GroupInfoResp group = bot.getGroupInfo(groupId, true).getData();
            if (group != null) registerGroup(groupId, group.getGroupName());
        }

        Long userId = event.getUserId();
        StrangerInfoResp user = bot.getStrangerInfo(userId, true).getData();
        if (user != null) registerUser(userId, user.getNickname());

        chain.doHandle(bot, event, cmd);
    }

    private void registerGroup(Long groupId, String groupName) {
        GroupPO group = groupService.getById(groupId);
        if (group == null) {
            group = new GroupPO(groupId, groupName);
        } else group.setName(groupName);
        groupService.saveOrUpdate(group);
        log.info("├─[RegisterHandler] 群聊已更新");
    }

    private void registerUser(Long userId, String userName) {
        UserPO user = userService.getById(userId);
        if (user == null) {
            user = new UserPO(userId, userName);
        } else user.setName(userName);
        userService.saveOrUpdate(user);
        log.info("├─[RegisterHandler] 用户已更新");
    }
}
