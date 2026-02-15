package com.mrtripop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

//  @Bean
//  public PasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder();
//  }

//  @Bean
//  public UserDetailsService userDetailsService(PasswordEncoder encoder) {
//    String password = encoder.encode("Password");
//    UserDetails userDetails = User.withUsername("user").password(password).roles("USER").build();
//    return new InMemoryUserDetailsManager(userDetails);
//  }

//  @Bean
//  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//    http.authorizeHttpRequests(registry -> registry.anyRequest().permitAll());
//
//    // Use HTTP basic authentication
//    http.httpBasic(Customizer.withDefaults());
//
//    // Disable CSRF
//    // in general, not require for stateless REST APIs
//    http.csrf(AbstractHttpConfigurer::disable);
//
//    return http.build();
//  }
}
