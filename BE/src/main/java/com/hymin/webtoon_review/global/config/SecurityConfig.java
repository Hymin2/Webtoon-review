package com.hymin.webtoon_review.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.global.handler.CustomAccessDeniedHandler;
import com.hymin.webtoon_review.global.handler.CustomAuthenticationEntryPoint;
import com.hymin.webtoon_review.global.security.JwtService;
import com.hymin.webtoon_review.global.security.filter.JwtAuthenticationFilter;
import com.hymin.webtoon_review.global.security.filter.JwtBlacklistFilter;
import com.hymin.webtoon_review.global.security.filter.UsernamePasswordAuthenticationFilter;
import com.hymin.webtoon_review.global.security.provider.JwtAuthenticationProvider;
import com.hymin.webtoon_review.global.security.provider.UsernamePasswordAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider(
        UserDetailsService userDetailsService
    ) {
        return new UsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(
        JwtService jwtService
    ) {
        return new JwtAuthenticationProvider(jwtService);
    }

    @Bean
    public AuthenticationManager authenticationManager(
        HttpSecurity http,
        UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider,
        JwtAuthenticationProvider jwtAuthenticationProvider
    ) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(usernamePasswordAuthenticationProvider)
            .authenticationProvider(jwtAuthenticationProvider)
            .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
        ObjectMapper objectMapper,
        AuthenticationManager authenticationManager
    ) {
        return new JwtAuthenticationFilter(objectMapper, authenticationManager);
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(
        AuthenticationManager authenticationManager
    ) {
        return new UsernamePasswordAuthenticationFilter(authenticationManager);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtBlacklistFilter jwtBlacklistFilter,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((authorizeRequests) -> {
                authorizeRequests
                    .requestMatchers("/stomp-chat/**").permitAll()
                    .requestMatchers("/users/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/webtoons/thumbnails/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().hasRole("USER");
            })
            .addFilterBefore(usernamePasswordAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtBlacklistFilter,
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling((handler) -> handler
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }
}
