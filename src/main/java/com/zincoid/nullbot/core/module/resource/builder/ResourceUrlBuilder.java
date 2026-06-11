package com.zincoid.nullbot.core.module.resource.builder;

public interface ResourceUrlBuilder {

    String from(Integer fileId);

    String from(String filePath);
}
