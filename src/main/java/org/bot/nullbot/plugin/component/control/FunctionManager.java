package org.bot.nullbot.plugin.component.control;

import org.bot.nullbot.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FunctionManager {
    private final DefaultConfig defaultConfig;
    private final Map<String, Boolean> enableFlags = new ConcurrentHashMap<>();

    public FunctionManager(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        // 使用反射获取所有字段
        loadConfigViaReflection();
    }

    private void loadConfigViaReflection() {
        try {
            // 获取DefaultConfig的所有字段
            Field[] fields = defaultConfig.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true); // 允许访问私有字段
                // 只处理布尔类型的字段
                if (field.getType().equals(boolean.class) ||
                        field.getType().equals(Boolean.class)) {
                    String fieldName = field.getName();
                    Boolean value = (Boolean) field.get(defaultConfig);
                    if (value != null) {
                        enableFlags.put(fieldName, value);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // 处理异常
            e.printStackTrace();
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
}
