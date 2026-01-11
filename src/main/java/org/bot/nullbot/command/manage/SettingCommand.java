package org.bot.nullbot.command.manage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.springframework.stereotype.Component;

@CommandMapping({"Setting", "群功能设置", "设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SettingCommand implements Command
{
}
