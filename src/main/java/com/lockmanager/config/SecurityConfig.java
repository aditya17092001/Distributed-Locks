package com.lockmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // dev only
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * In-memory user for testing.
     * Use external auth (JWT, OAuth2, etc.) in production.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.withUsername("abc")
                .password("abc")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Dev-only password encoder.
     * Replace with BCryptPasswordEncoder in real applications.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Updated lambda-style security configuration (Spring Security 6.1+)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless APIs
                .csrf(csrf -> csrf.disable())

                // Authorize requests using the new lambda style
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/welcome", "/actuator/**").permitAll()
                        .requestMatchers("/lock/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Use HTTP Basic auth with the new Customizer syntax
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
