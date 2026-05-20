package com.zincoid.nullbot.component.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Invoker {

    private final ApplicationContext applicationContext;

    // public String test() { return "test-non"; }
    // public String test(int a) { return "test-int " + a; }
    // public String test(int a, int b) { return "test-int-plus " + (a + b); }
    // public String test(double a, double b) { return "test-double-plus" + (a + b); }

    public Object invokeSpringMethod(String beanName, String methodName,
                                     Object... args) throws Exception {
        // 从 Spring 容器获取 Bean
        Object bean = applicationContext.getBean(beanName);
        Method[] methods = bean.getClass().getMethods();

        List<String> errors = new ArrayList<>();
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) continue;
            if (method.getParameterCount() != args.length) continue;
            try {
                // 处理无参数方法
                if (args.length == 0) return method.invoke(bean);
                // 处理有参数方法
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < args.length; i++)
                    args[i] = convertFromString((String) args[i], paramTypes[i]);
                return method.invoke(bean, args);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                errors.add("- " + targetException.getMessage());
            } catch (Exception e) {
                errors.add("- " + e.getMessage());
            }
        }

        if (errors.isEmpty())
            throw new NoSuchMethodException(
                    String.format("Method '%s' not found with %d args", methodName, args.length));
        else
            throw new RuntimeException(
                    String.format("All %d method(s) failed:\n%s", errors.size(), String.join(";\n", errors)));
    }

    private Object convertFromString(String value, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(value);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(value);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(value);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(value);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (targetType == Short.class || targetType == short.class) {
                return Short.parseShort(value);
            } else if (targetType == Byte.class || targetType == byte.class) {
                return Byte.parseByte(value);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Cannot convert '%s' to %s", value, targetType.getSimpleName()));
        }
        throw new IllegalArgumentException(
                String.format("Unsupported conversion from String to %s", targetType.getSimpleName()));
    }
}
