package io.datacraft.pipeline.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.datacraft.pipeline.infrastructure.persistence.entity.PipelineEdgeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineEdgeMapper extends BaseMapper<PipelineEdgeEntity> {
}
