package com.zincoid.nullbot.core.service.render;

public interface CapturingService {

    String essence(String weapon);

    String prtsAny(String keyword);

    String prtsOpt(String option, String keyword);
}
