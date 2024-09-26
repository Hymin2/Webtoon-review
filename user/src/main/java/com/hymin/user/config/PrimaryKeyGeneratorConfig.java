package com.hymin.user.config;

import com.hymin.common.utils.PrimaryKeyGenerator;
import com.hymin.common.utils.SnowFlakeGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrimaryKeyGeneratorConfig {

    @Bean
    public PrimaryKeyGenerator primaryKeyGenerator() {
        return new SnowFlakeGenerator(1L);
    }
}
