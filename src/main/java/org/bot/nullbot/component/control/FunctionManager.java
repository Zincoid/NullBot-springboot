package org.bot.nullbot.component.control;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.FunctionControl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class FunctionManager {

    private final ApplicationContext applicationContext;

    private final Map<String, Boolean> enableFlags = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (beanName.equals("functionManager")) continue;
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) continue;
            FunctionControl classAnno = AnnotationUtils
                    .findAnnotation(beanType, FunctionControl.class);
            if (classAnno != null)
                enableFlags.put(classAnno.value(), true);
            Arrays.stream(beanType.getDeclaredMethods())
                    .forEach(method -> {
                        FunctionControl methodAnno = AnnotationUtils
                                .findAnnotation(method, FunctionControl.class);
                        if (methodAnno != null)
                            enableFlags.put(methodAnno.value(), methodAnno.enabled());
                    });
        }
    }

    public boolean isEnabled(String function) {
        Boolean enabled = enableFlags.get(function);
        if (enabled == null)
            throw new NullPointerException("未定义的功能");
        return enabled;
    }

    public boolean setEnabled(String function, boolean enabled) {
        isEnabled(function);
        enableFlags.put(function, enabled);
        return enabled;
    }

    public boolean switchEnabled(String function) {
        boolean enabled = isEnabled(function);
        return setEnabled(function, !enabled);
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
