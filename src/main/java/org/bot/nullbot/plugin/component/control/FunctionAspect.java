package org.bot.nullbot.plugin.component.control;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bot.nullbot.annotation.FunctionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class FunctionAspect
{
    private static final Logger logger = LoggerFactory.getLogger(FunctionAspect.class);
    private final FunctionManager functionManager;

    @Around("@annotation(function)")
    public Object checkFunction(ProceedingJoinPoint joinPoint, FunctionControl function) throws Throwable {
        boolean enabled = functionManager.isEnabled(function.config());
        if (enabled) {
            // logger.info("◉ [FunctionManager] {} 已启用", function.config());
            return joinPoint.proceed();
        } else {
            // logger.info("◉ [FunctionManager] {} 未启用", function.config());
            return null;
        }
    }
}
