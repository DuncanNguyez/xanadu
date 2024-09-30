package identify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author Duncan Nguyen
 */
@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig {
  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/assets/**", "/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(formLogin -> formLogin.loginPage("/login"))
        .oauth2Login(formLogin -> formLogin.loginPage("/login"))
        .build();
  }
}
