package com.datacraft.datasource.domain;

import java.util.List;
import java.util.Optional;

public interface DatasourceRepository {
    List<Datasource> findAll();
    Optional<Datasource> findById(Long id);
    boolean existsByName(String name, Long excludingId);
    Datasource save(Datasource datasource);
    void deleteById(Long id);
}
