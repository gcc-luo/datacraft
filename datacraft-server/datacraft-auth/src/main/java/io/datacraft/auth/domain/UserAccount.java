package io.datacraft.auth.domain;

import java.util.List;

public record UserAccount(
        Long id,
        String username,
        String displayName,
        String passwordHash,
        boolean enabled,
        List<String> roleCodes) {

    public boolean canAuthenticate() {
        return enabled && username != null && passwordHash != null;
    }
}
