package com.zincoid.nullbot.core.component.ai.chat.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.*;

@Getter
public class ToolDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String name;
    private final String description;
    private final ObjectNode parameters;

    private ToolDef(String name, String description, ObjectNode parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public static Builder builder(String name, String description) {
        return new Builder(name, description);
    }

    public static JsonNode parseArgs(String jsonArgs) {
        try {
            return MAPPER.readTree(jsonArgs);
        } catch (Exception e) {
            throw new RuntimeException("JSON参数解析失败: " + jsonArgs, e);
        }
    }

    public static class Builder {
        private final String name;
        private final String description;
        private final List<ParamSpec> params = new ArrayList<>();
        private final Set<String> requiredNames = new LinkedHashSet<>();

        private Builder(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public Builder addString(String name, String desc) {
            params.add(new ParamSpec(name, "string", desc, null));
            return this;
        }

        public Builder addString(String name, String desc, boolean required) {
            params.add(new ParamSpec(name, "string", desc, null));
            if (required) requiredNames.add(name);
            return this;
        }

        public Builder addEnum(String name, String desc, Set<String> values) {
            params.add(new ParamSpec(name, "string", desc, new TreeSet<>(values)));
            return this;
        }

        public Builder addEnum(String name, String desc, Set<String> values, boolean required) {
            params.add(new ParamSpec(name, "string", desc, new TreeSet<>(values)));
            if (required) requiredNames.add(name);
            return this;
        }

        public ToolDef build() {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("type", "object");

            ObjectNode props = MAPPER.createObjectNode();
            for (ParamSpec spec : params) {
                ObjectNode prop = MAPPER.createObjectNode();
                prop.put("type", spec.type);
                prop.put("description", spec.description);
                if (spec.enumValues != null) {
                    ArrayNode arr = MAPPER.createArrayNode();
                    spec.enumValues.forEach(arr::add);
                    prop.set("enum", arr);
                }
                props.set(spec.name, prop);
            }
            root.set("properties", props);

            if (!requiredNames.isEmpty()) {
                ArrayNode requiredArr = MAPPER.createArrayNode();
                requiredNames.forEach(requiredArr::add);
                root.set("required", requiredArr);
            }

            return new ToolDef(name, description, root);
        }
    }

    private record ParamSpec(String name, String type, String description, Set<String> enumValues) {}
}
