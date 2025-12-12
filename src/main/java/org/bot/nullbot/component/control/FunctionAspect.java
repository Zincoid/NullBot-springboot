package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bot.nullbot.annotation.FunctionControl;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class FunctionAspect
{
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
