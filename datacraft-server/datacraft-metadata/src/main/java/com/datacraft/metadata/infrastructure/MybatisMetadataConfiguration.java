package com.datacraft.metadata.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.datacraft.metadata.infrastructure.persistence.mapper")
public class MybatisMetadataConfiguration {
}
