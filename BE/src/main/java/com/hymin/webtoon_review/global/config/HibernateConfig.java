package com.hymin.webtoon_review.global.config;

import com.hymin.webtoon_review.global.inspector.QueryInspector;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (hibernateProperties) -> {
            hibernateProperties.put(AvailableSettings.STATEMENT_INSPECTOR, new QueryInspector());
        };
    }
}
