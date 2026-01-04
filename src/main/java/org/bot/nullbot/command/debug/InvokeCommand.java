package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@CommandMapping({"Invoke", "调用"})
@Component
@Slf4j
@RequiredArgsConstructor
public class InvokeCommand implements Command {

    private final ApplicationContext applicationContext;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.size() < 2) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[调用] ❌参数不足", false);
                log.info("\t\t\t\t├─[Invoke] 参数不足");
                return;
            }

            String beanName = params.get(0);
            String methodName = params.get(1);

            // 获取参数并转换为数组
            Object[] args = new Object[0];
            if (params.size() > 2) {
                args = params.subList(2, params.size()).toArray();
            }

            try {
                Object result = invokeSpringMethod(applicationContext, beanName, methodName, args);
                String res = result != null ? result.toString() : "null";
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), res, false);
                log.info("\t\t\t\t├─[Invoke] 调用结果 -> {}", res);
            } catch (Exception e) {
                String errorMsg = "[调用] ❌错误: " + e.getMessage();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), errorMsg, false);
                log.info("\t\t\t\t├─[Invoke] 调用失败", e);
            }
        } else {
            log.info("\t\t\t\t├─[Invoke] 未设计 非群消息事件响应方式");
        }
    }

    public Object invokeSpringMethod(ApplicationContext context, String beanName,
                                     String methodName, Object... args) throws Exception {
        // 从Spring容器获取Bean
        Object bean = context.getBean(beanName);

        // 处理无参数方法
        if (args == null || args.length == 0) {
            Method method = bean.getClass().getMethod(methodName);
            return method.invoke(bean);
        }

        // 获取参数类型
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                paramTypes[i] = Object.class;
            } else {
                paramTypes[i] = args[i].getClass();
                // 注意：对于基本类型需要特殊处理
                if (args[i] instanceof Integer) {
                    paramTypes[i] = int.class;
                } else if (args[i] instanceof Double) {
                    paramTypes[i] = double.class;
                } else if (args[i] instanceof Boolean) {
                    paramTypes[i] = boolean.class;
                } else if (args[i] instanceof Long) {
                    paramTypes[i] = long.class;
                } else if (args[i] instanceof Float) {
                    paramTypes[i] = float.class;
                } else if (args[i] instanceof Short) {
                    paramTypes[i] = short.class;
                } else if (args[i] instanceof Byte) {
                    paramTypes[i] = byte.class;
                } else if (args[i] instanceof Character) {
                    paramTypes[i] = char.class;
                }
            }
        }

        try {
            // 尝试精确匹配
            Method method = bean.getClass().getMethod(methodName, paramTypes);
            return method.invoke(bean, args);
        } catch (NoSuchMethodException e) {
            // 如果找不到精确匹配，尝试智能查找
            return invokeMethodSmart(bean, methodName, args);
        }
    }

    /**
     * 智能方法调用，处理参数类型不匹配的情况
     */
    private Object invokeMethodSmart(Object bean, String methodName, Object... args) throws Exception {
        Method[] methods = bean.getClass().getMethods();

        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            if (method.getParameterCount() != args.length) {
                continue;
            }

            // 检查参数类型是否兼容
            Class<?>[] paramTypes = method.getParameterTypes();
            if (isParametersCompatible(paramTypes, args)) {
                // 转换参数类型
                Object[] convertedArgs = convertArguments(args, paramTypes);
                return method.invoke(bean, convertedArgs);
            }
        }

        throw new NoSuchMethodException(
                String.format("Method %s not found with compatible parameters for types: %s",
                        methodName, getArgumentTypesString(args)));
    }

    /**
     * 检查参数类型是否兼容
     */
    private boolean isParametersCompatible(Class<?>[] paramTypes, Object[] args) {
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] == null) {
                // null 可以传递给任何非基本类型
                if (paramTypes[i].isPrimitive()) {
                    return false;
                }
            } else {
                Class<?> argType = args[i].getClass();
                if (!paramTypes[i].isAssignableFrom(argType)) {
                    // 检查基本类型和包装类型的兼容性
                    if (!isPrimitiveCompatible(paramTypes[i], argType)) {
                        // 检查字符串转换
                        if (!(args[i] instanceof String && canConvertFromString(paramTypes[i]))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * 检查基本类型兼容性
     */
    private boolean isPrimitiveCompatible(Class<?> target, Class<?> source) {
        return (target == int.class && source == Integer.class) ||
                (target == long.class && source == Long.class) ||
                (target == double.class && source == Double.class) ||
                (target == float.class && source == Float.class) ||
                (target == boolean.class && source == Boolean.class) ||
                (target == char.class && source == Character.class) ||
                (target == byte.class && source == Byte.class) ||
                (target == short.class && source == Short.class);
    }

    /**
     * 检查是否可以从字符串转换
     */
    private boolean canConvertFromString(Class<?> targetType) {
        return targetType == String.class ||
                targetType == Integer.class || targetType == int.class ||
                targetType == Long.class || targetType == long.class ||
                targetType == Double.class || targetType == double.class ||
                targetType == Float.class || targetType == float.class ||
                targetType == Boolean.class || targetType == boolean.class ||
                targetType == Short.class || targetType == short.class ||
                targetType == Byte.class || targetType == byte.class;
    }

    /**
     * 转换参数
     */
    private Object[] convertArguments(Object[] args, Class<?>[] paramTypes) {
        Object[] converted = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                converted[i] = null;
            } else if (paramTypes[i].isAssignableFrom(args[i].getClass())) {
                converted[i] = args[i];
            } else if (args[i] instanceof String) {
                // 尝试从字符串转换
                converted[i] = convertFromString((String) args[i], paramTypes[i]);
            } else {
                // 无法转换
                converted[i] = args[i];
            }
        }

        return converted;
    }

    /**
     * 从字符串转换
     */
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

    /**
     * 获取参数类型字符串（用于错误信息）
     */
    private String getArgumentTypesString(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }

        List<String> typeNames = new ArrayList<>();
        for (Object arg : args) {
            typeNames.add(arg != null ? arg.getClass().getSimpleName() : "null");
        }

        return String.join(", ", typeNames);
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Invoke 命令
                功能: 反射调用方法
                限权: %d
                格式: Invoke [Bean名称] [方法名] [参数...]
                中文命令: 调用""", getAccess()
        );
    }
}