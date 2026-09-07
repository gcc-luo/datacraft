package io.datacraft.datasource.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.datacraft.api.datasource.DatasourceStatus;
import io.datacraft.api.datasource.DatasourceType;
import io.datacraft.datasource.domain.Datasource;
import io.datacraft.datasource.domain.DatasourceRepository;
import io.datacraft.datasource.infrastructure.persistence.entity.DatasourceEntity;
import io.datacraft.datasource.infrastructure.persistence.mapper.DatasourceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDatasourceRepository implements DatasourceRepository {
    private final DatasourceMapper mapper;

    public JdbcDatasourceRepository(DatasourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Datasource> findAll() {
        QueryWrapper<DatasourceEntity> query = new QueryWrapper<>();
        query.orderByDesc("created_at").orderByAsc("id");
        return mapper.selectList(query).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Datasource> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name, Long excludingId) {
        QueryWrapper<DatasourceEntity> query = new QueryWrapper<>();
        query.eq("name", name);
        if (excludingId != null) {
            query.ne("id", excludingId);
        }
        return mapper.selectCount(query) > 0;
    }

    @Override
    public Datasource save(Datasource datasource) {
        DatasourceEntity entity = toEntity(datasource);
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    private Datasource toDomain(DatasourceEntity entity) {
        return new Datasource(entity.getId(), entity.getName(), DatasourceType.valueOf(entity.getType()), entity.getHost(),
                entity.getPort(), entity.getDatabaseName(), entity.getUsername(), entity.getPasswordCiphertext(), entity.getRemark(),
                DatasourceStatus.valueOf(entity.getStatus()), entity.getLastTestedAt(), entity.getLastTestLatencyMs(),
                entity.getLastTestMessage(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private DatasourceEntity toEntity(Datasource datasource) {
        DatasourceEntity entity = new DatasourceEntity();
        entity.setId(datasource.id());
        entity.setName(datasource.name());
        entity.setType(datasource.type().name());
        entity.setHost(datasource.host());
        entity.setPort(datasource.port());
        entity.setDatabaseName(datasource.databaseName());
        entity.setUsername(datasource.username());
        entity.setPasswordCiphertext(datasource.passwordCiphertext());
        entity.setRemark(datasource.remark());
        entity.setStatus(datasource.status().name());
        entity.setLastTestedAt(datasource.lastTestedAt());
        entity.setLastTestLatencyMs(datasource.lastTestLatencyMs());
        entity.setLastTestMessage(datasource.lastTestMessage());
        entity.setCreatedAt(datasource.createdAt());
        entity.setUpdatedAt(datasource.updatedAt());
        return entity;
    }
}
