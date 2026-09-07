package com.datacraft.api.auth;

import java.util.List;

public record UserSummary(Long id, String username, String displayName, List<String> roles) {
}
