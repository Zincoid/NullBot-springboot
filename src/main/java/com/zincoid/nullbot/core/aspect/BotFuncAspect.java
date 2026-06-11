package com.zincoid.nullbot.core.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import com.zincoid.nullbot.core.annotation.FunctionControl;
import com.zincoid.nullbot.core.module.control.FunctionManager;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BotFuncAspect {

    private final FunctionManager functionManager;

    @Around("@annotation(function)")
    public Object check(ProceedingJoinPoint joinPoint, FunctionControl function) throws Throwable {
        if (functionManager.isEnabled(function.value()))
            return joinPoint.proceed();
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
