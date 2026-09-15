package com.zincoid.nullbot.core.model.data.vo;

import lombok.Data;

import java.util.List;

@Data
public class ModelVO {

    private String active;
    private List<ProviderVO> providers;

    @Data
    public static class ProviderVO {
        private String name;
        private String model;

        public ProviderVO(String name, String model) {
            this.name = name;
            this.model = model;
        }
    }
}
