package org.bot.nullbot.component.control;

import org.bot.nullbot.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FunctionManager  // 全局控制
{
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
