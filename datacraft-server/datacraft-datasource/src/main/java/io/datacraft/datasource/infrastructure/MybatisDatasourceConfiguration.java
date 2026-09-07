package io.datacraft.datasource.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.datacraft.datasource.infrastructure.persistence.mapper")
public class MybatisDatasourceConfiguration {
}
