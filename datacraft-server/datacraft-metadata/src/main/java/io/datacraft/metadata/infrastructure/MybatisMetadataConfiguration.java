package io.datacraft.metadata.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.datacraft.metadata.infrastructure.persistence.mapper")
public class MybatisMetadataConfiguration {
}
