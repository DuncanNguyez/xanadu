package identify.config;

import identify.model.User;
import identify.reponsitory.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {
  String EMAIL_KEY_ON_ID_TOKEN = "email";
  String GITHUB_KEY_ON_ID_TOKEN = "github";
  String FIRST_NAME_KEY_ON_ID_TOKEN = "first_name";
  String LAST_NAME_KEY_ON_ID_TOKEN = "last_name";
  String AVATAR_URL_KEY_ON_ID_TOKEN = "avatar_url";

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
      throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        .authorizationEndpoint(
            authorizationEndpoint -> authorizationEndpoint.consentPage("/oauth2/consent"))
        .oidc(Customizer.withDefaults());

    http.cors(Customizer.withDefaults())
        .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()))
        .exceptionHandling(
            (exceptions) ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
    return http.build();
  }

  @Bean
  public OAuth2AuthorizationConsentService authorizationConsentService() {
    return new InMemoryOAuth2AuthorizationConsentService();
  }

  /** Customizer that adds user info (email, GitHub, name, etc.) to the id token. */
  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer(
      UserRepository userRepository) {
    return context -> {
      if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
        var key = context.getPrincipal().getName();
        User user =
            userRepository
                .findByEmailOrGithub(key, key)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put(this.EMAIL_KEY_ON_ID_TOKEN, user.getEmail());
        userInfo.put(this.GITHUB_KEY_ON_ID_TOKEN, user.getGithub());
        userInfo.put(this.FIRST_NAME_KEY_ON_ID_TOKEN, user.getFirstname());
        userInfo.put(this.LAST_NAME_KEY_ON_ID_TOKEN, user.getLastname());
        userInfo.put(this.AVATAR_URL_KEY_ON_ID_TOKEN, user.getAvatarUrl());

        context.getClaims().claims(claims -> claims.putAll(userInfo));
      }
    };
  }
}
