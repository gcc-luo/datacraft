package io.datacraft.auth.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.datacraft.auth.infrastructure.persistence.mapper")
public class MybatisAuthConfiguration {
}
