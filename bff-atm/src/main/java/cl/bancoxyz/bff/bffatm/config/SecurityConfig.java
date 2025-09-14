package cl.bancoxyz.bff.bffatm.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(
        a -> a.requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated());
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public UserDetailsService users() {
    return new InMemoryUserDetailsManager(
        User.withUsername("atm_user").password("{noop}bff-atm-123").roles("USER").build(),
        User.withUsername("atm_admin").password("{noop}bff-atm-admin").roles("ADMIN","USER").build());
  }
}
