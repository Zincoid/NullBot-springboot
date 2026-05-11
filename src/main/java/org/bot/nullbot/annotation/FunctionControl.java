package org.bot.nullbot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionControl {  // Aspect
    String id();  // 功能名标识符
    boolean enabled() default true;  // 默认启用状态
}
