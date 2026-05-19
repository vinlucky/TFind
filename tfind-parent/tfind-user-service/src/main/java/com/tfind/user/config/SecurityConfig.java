package com.tfind.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/user/login", "/api/user/register").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/user/*").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/user/*/restore").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/user/*/admin").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/user/*/admin").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/user/deleted").hasRole("ADMIN")
                .antMatchers("/api/user/**").authenticated()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
