package io.datacraft.quality.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("io.datacraft.quality.infrastructure.persistence.mapper")
public class MybatisQualityConfiguration {
}
