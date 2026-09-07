package com.datacraft.auth.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.datacraft.auth.infrastructure.persistence.mapper")
public class MybatisAuthConfiguration {
}
