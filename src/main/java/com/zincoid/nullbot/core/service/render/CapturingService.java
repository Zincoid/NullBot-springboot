package com.zincoid.nullbot.core.service.render;

public interface CapturingService {

    String prtsAny(String keyword);

    String prtsOpt(String option, String keyword);

    String ai(String option);
}
