package com.datacraft.pipeline.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datacraft.pipeline.infrastructure.persistence.entity.PipelineEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineMapper extends BaseMapper<PipelineEntity> {
}
