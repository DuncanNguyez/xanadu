package identify.config;

import identify.service.CustomOAuth2UserService;
import identify.service.CustomOidcUserService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * @author Duncan Nguyen
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class DefaultSecurityConfig {
  @Autowired private CustomOAuth2UserService customOAuth2UserService;
  @Autowired private CustomOidcUserService customOidcUserService;

  public final AuthenticationFailureHandler authenticationFailureHandler =
      (request, response, exception) -> {
        if (exception instanceof InternalAuthenticationServiceException) {
          response.sendRedirect("/login?error=Internal Server Error");
        } else {
          String errorMessage = exception.getMessage();
          String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
          response.sendRedirect("/login?error=" + encodedMessage);
        }
      };

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
        .formLogin(
            formLogin -> formLogin.loginPage("/login").failureHandler(authenticationFailureHandler))
        .oauth2Login(
            formLogin ->
                formLogin
                    .loginPage("/login")
                    .userInfoEndpoint(
                        d ->
                            d.userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService))
                    .failureHandler(authenticationFailureHandler))
        .build();
  }
}
