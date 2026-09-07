package com.datacraft.auth.domain;

public record Role(Long id, String code, String name, boolean enabled) {
}
