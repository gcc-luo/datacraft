package io.datacraft.pipeline.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.datacraft.pipeline.infrastructure.persistence.mapper")
public class MybatisPipelineConfiguration {
}
