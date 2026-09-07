package io.datacraft.datasource.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.datasource.infrastructure.persistence.entity.DatasourceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DatasourceMapper extends BaseMapper<DatasourceEntity> {
}
