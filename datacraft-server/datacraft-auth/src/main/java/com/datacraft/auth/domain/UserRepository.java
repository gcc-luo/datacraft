package com.datacraft.auth.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByUsername(String username);

    UserAccount insert(UserAccount account);

    void assignRole(Long userId, String roleCode);
}
