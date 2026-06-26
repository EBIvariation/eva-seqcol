package uk.ac.ebi.eva.evaseqcol.controller.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String ROLE_ADMIN = "ADMIN";

    @Value("${controller.auth.admin.username}")
    private String USERNAME_ADMIN;

    @Value("${controller.auth.admin.password}")
    private String PASSWORD_ADMIN;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager(
                User.withUsername(USERNAME_ADMIN)
                        .password(passwordEncoder().encode(PASSWORD_ADMIN))
                        .roles(ROLE_ADMIN)
                        .build()
        );
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/collection/**").permitAll();
                    auth.requestMatchers("/comparison/**").permitAll();
                    auth.requestMatchers("/admin/**").hasRole(ROLE_ADMIN);
                    auth.anyRequest().permitAll();
                })
                .httpBasic(Customizer.withDefaults())
                .build();
    }

}
