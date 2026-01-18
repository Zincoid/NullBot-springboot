package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@CommandMapping({"Invoke", "调用"})
@Component
@Slf4j
@RequiredArgsConstructor
public class InvokeCommand implements Command
{
    private final ApplicationContext applicationContext;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.size() < 2) throw new NullBotMsgException("[Spring] ❌未指定Bean和Method");

            String beanName = params.get(0);
            String methodName = params.get(1);
            Object[] args = new Object[0];
            if (params.size() > 2) args = params.subList(2, params.size()).toArray();

            try {
                Object result = invokeSpringMethod(applicationContext, beanName, methodName, args);
                String res = result != null ? result.toString() : "null";
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Spring] ✅方法调用成功\nThe method returned:\n" + res, false);
                log.info("\t\t\t\t├─[Invoke] 调用结果 -> {}", res);
            } catch (Exception e) {
                throw new NullBotMsgException("[Spring] ⚠️方法调用失败\n" + e.getMessage());
            }
        } else
            throw new NullBotLogException("[Spring] ❌未设计 - 非群消息事件响应方式");
    }

    public Object invokeSpringMethod(ApplicationContext context, String beanName,
                                     String methodName, Object... args) throws Exception {
        // 从Spring容器获取Bean
        Object bean = context.getBean(beanName);
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

    @Override
    public Integer getAccess() {
        return 2;
    }

    // public String test() { return "test-non"; }
    // public String test(int a) { return "test-int " + a; }
    // public String test(int a, int b) { return "test-int-plus " + (a + b); }
    // public String test(double a, double b) { return "test-double-plus" + (a + b); }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Invoke 命令
                功能: 反射调用Spring方法
                限权: %d 级
                格式: Invoke [Bean名] [方法名] [参数...]
                别名: 调用""", getAccess()
        );
    }
}