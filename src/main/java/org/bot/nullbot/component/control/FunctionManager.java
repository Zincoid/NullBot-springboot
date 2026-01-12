package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.config.DefaultConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FunctionManager  // 全局控制
{
    private final ApplicationContext applicationContext;

    private final DefaultConfig defaultConfig;
    private final Map<String, Boolean> enableFlags = new ConcurrentHashMap<>();

    public FunctionManager(ApplicationContext applicationContext, DefaultConfig defaultConfig) {
        this.applicationContext = applicationContext;
        this.defaultConfig = defaultConfig;
        // loadConfigViaDefaultConfig();
        loadConfigViaAnnotation();
    }

    private void loadConfigViaDefaultConfig() {
        try {
            // 获取DefaultConfig的所有字段
            Field[] fields = defaultConfig.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                // 只处理布尔类型的字段
                if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                    String fieldName = field.getName();
                    Boolean value = (Boolean) field.get(defaultConfig);
                    if (value != null) {
                        // enableFlags.put(fieldName, value);
                        enableFlags.put(fieldName, true);  // 全局管理 默认修改为全部启用
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void loadConfigViaAnnotation() {
        List<String> configs = new ArrayList<>();
        // 扫描类级别的注解
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            // 获取类上的注解
            FunctionControl classAnnotation = AnnotationUtils.findAnnotation(
                    beanClass, FunctionControl.class);
            if (classAnnotation != null) {
                enableFlags.put(classAnnotation.config(), true);
            }
            // 扫描方法级别的注解
            Arrays.stream(beanClass.getDeclaredMethods())
                    .forEach(method -> {
                        FunctionControl methodAnnotation = AnnotationUtils.findAnnotation(
                                method, FunctionControl.class);
                        if (methodAnnotation != null) {
                            enableFlags.put(methodAnnotation.config(), true);
                        }
                    });
        }
    }

    public Boolean isEnabled(String functionName) {
        return enableFlags.get(functionName);
    }

    public void setEnabled(String functionName, boolean enabled) {
        enableFlags.put(functionName, enabled);
    }

    public Boolean switchEnabled(String functionName) {
        Boolean flag = enableFlags.get(functionName);
        if (flag != null) {
            enableFlags.put(functionName, !flag);
            return !flag;
        }
        return null;
    }

    public String getStatus() {
        StringBuilder status = new StringBuilder(" ◉ Global 设置\n");
        Iterator<Map.Entry<String, Boolean>> iterator = enableFlags.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Boolean> entry = iterator.next();
            char prefix = iterator.hasNext() ? '├' : '└';
            status.append(prefix).append(' ').append(entry.getKey()).append(" - ")
                    .append(entry.getValue() ? "ON" : "OFF");
            if (iterator.hasNext()) {
                status.append('\n');
            }
        }
        return status.toString();
    }
}
