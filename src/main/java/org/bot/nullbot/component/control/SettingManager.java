package org.bot.nullbot.component.control;

import org.bot.nullbot.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SettingManager
{
    private final DefaultConfig defaultConfig;
    private final Map<String, Boolean> enableFlags = new ConcurrentHashMap<>();

    public SettingManager(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        loadConfigViaReflection();
    }

    private void loadConfigViaReflection() {
        try {
            // 获取DefaultConfig的所有字段
            Field[] fields = defaultConfig.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
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

    public String getStatus() {
        StringBuilder status = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : enableFlags.entrySet()) {
            status.append('\n').append(entry.getKey()).append(" -> ").append(entry.getValue() ? "ON" : "OFF");
        }
        return status.toString();
    }
}
