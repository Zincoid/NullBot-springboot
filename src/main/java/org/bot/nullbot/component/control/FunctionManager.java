package org.bot.nullbot.component.control;

import jakarta.annotation.PostConstruct;
import org.bot.nullbot.annotation.FunctionControl;
import org.bot.nullbot.config.DefaultProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FunctionManager {
    private final ApplicationContext applicationContext;
    private final DefaultProperties defaultProperties;
    private final Map<String, Boolean> enableFlags = new ConcurrentHashMap<>();

    public FunctionManager(ApplicationContext applicationContext, DefaultProperties defaultProperties) {
        this.applicationContext = applicationContext;
        this.defaultProperties = defaultProperties;
        // 不在构造函数中初始化 enableFlags 避免循环依赖
    }

    @PostConstruct
    public void init() {
        // loadPropsViaDefault();
        loadPropsViaAnnotation();
    }

    private void loadPropsViaDefault() {
        try {
            org.springframework.beans.BeanWrapper wrapper =
                    new org.springframework.beans.BeanWrapperImpl(defaultProperties);

            for (java.beans.PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
                if (pd.getPropertyType() == Boolean.class || pd.getPropertyType() == boolean.class) {
                    Object value = wrapper.getPropertyValue(pd.getName());
                    if (value != null) {
                        enableFlags.put(pd.getName(), true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPropsViaAnnotation() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (beanName.equals("functionManager")) {
                continue;
            }
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) continue;
            // 扫描类注解
            FunctionControl classAnnotation = AnnotationUtils.findAnnotation(
                    beanType, FunctionControl.class);
            if (classAnnotation != null) {
                enableFlags.put(classAnnotation.config(), true);
            }
            // 扫描方法注解
            Arrays.stream(beanType.getDeclaredMethods())
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
