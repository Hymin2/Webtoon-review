package com.hymin.webtoon_review.global.config;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider() {
        return new UsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(usernamePasswordAuthenticationProvider())
            .authenticationProvider(jwtAuthenticationProvider())
            .build();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(
        HttpSecurity http) throws Exception {
        return new UsernamePasswordAuthenticationFilter(authenticationManager(http));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .cors((cors) -> cors.disable())
            .authorizeHttpRequests((authorizeRequests) -> {
                authorizeRequests.requestMatchers("/users/**").permitAll();
                authorizeRequests.anyRequest().hasRole("USER");
            })
            .addFilterAt(usernamePasswordAuthenticationFilter(http),
                BasicAuthenticationFilter.class);

        return http.build();
    }
}
