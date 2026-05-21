package com.zincoid.nullbot.core.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;

@Data
@AllArgsConstructor
public class StompPrincipal implements Principal {

    private final Long userId;
    private final String userName;

    @Override
    public String getName() {
        return userId.toString();
    }
}
