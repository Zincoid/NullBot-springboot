package com.zincoid.nullbot.core.component.resource.builder;

public interface ResourceUrlBuilder {

    String from(Integer fileId);

    String from(String filePath);
}
