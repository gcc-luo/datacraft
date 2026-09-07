package com.datacraft.datasource.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.datacraft.datasource.infrastructure.persistence.mapper")
public class MybatisDatasourceConfiguration {
}
