package com.datacraft.pipeline.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.datacraft.pipeline.infrastructure.persistence.mapper")
public class MybatisPipelineConfiguration {
}
