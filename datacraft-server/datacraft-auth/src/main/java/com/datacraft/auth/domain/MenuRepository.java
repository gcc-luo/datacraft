package com.datacraft.auth.domain;

import java.util.Collection;
import java.util.List;

public interface MenuRepository {
    List<MenuItem> findForRoles(Collection<String> roleCodes);
}
