package io.datacraft.quality.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.quality.infrastructure.persistence.entity.QualityResultEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QualityResultMapper extends BaseMapper<QualityResultEntity> {
}
