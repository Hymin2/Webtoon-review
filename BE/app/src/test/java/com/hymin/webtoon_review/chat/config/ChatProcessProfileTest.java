package com.hymin.webtoon_review.chat.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

class ChatProcessProfileTest {

    @ParameterizedTest
    @ValueSource(strings = "chat-persister")
    void 백그라운드_채팅_프로세스는_웹_서버_없이_실행된다(String profile) {
        SpringApplication application = new SpringApplication(EmptyConfiguration.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);

        try (ConfigurableApplicationContext context = application.run(
            "--spring.profiles.active=" + profile
        )) {
            assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
            assertThat(context.getEnvironment().getProperty("spring.main.web-application-type"))
                .isEqualTo("none");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EmptyConfiguration {
    }
}
