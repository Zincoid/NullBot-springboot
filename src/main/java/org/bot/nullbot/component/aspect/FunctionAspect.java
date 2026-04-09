package org.bot.nullbot.component.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.component.control.FunctionManager;
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
        boolean enabled = functionManager.isEnabled(function.id());
        if (enabled) {
            // logger.info("◉ [FunctionManager] {} 已启用", function.config());
            return joinPoint.proceed();
        } else {
            // logger.info("◉ [FunctionManager] {} 未启用", function.config());
            // 根据方法返回类型返回默认值
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();
            if (returnType.isPrimitive()) {
                if (returnType == boolean.class) return false;
                if (returnType == byte.class) return (byte) 0;
                if (returnType == char.class) return '\u0000';
                if (returnType == short.class) return (short) 0;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == float.class) return 0.0f;
                if (returnType == double.class) return 0.0d;
            }
            return null;
        }
    }
}
