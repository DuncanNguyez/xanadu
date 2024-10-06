package identify.config;

import identify.model.User;
import identify.reponsitory.UserRepository;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.util.ClassUtils;

@ExtendWith(MockitoExtension.class)
public class AuthorizationServerConfigTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private AuthorizationServerConfig authorizationServerConfig;
  @Captor ArgumentCaptor<Consumer<Map<String, Object>>> claimsCaptor;

  @Test
  public void assertOrderHighestPrecedence() {
    Method authorizationServerSecurityFilterChainMethod =
        ClassUtils.getMethod(
            AuthorizationServerConfig.class,
            "authorizationServerSecurityFilterChain",
            HttpSecurity.class);
    Integer order = OrderUtils.getOrder(authorizationServerSecurityFilterChainMethod);
    Assertions.assertThat(order).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
  }

  @Nested
  class OAuth2TokenCustomizerTest {
    private JwtEncodingContext jwtEncodingContext;

    @BeforeEach
    public void setUp() {
      this.jwtEncodingContext = Mockito.mock(JwtEncodingContext.class);
      var oauth2TokenType = Mockito.mock(OAuth2TokenType.class);
      Mockito.when(jwtEncodingContext.getTokenType()).thenReturn(oauth2TokenType);
      Mockito.when(jwtEncodingContext.getTokenType().getValue())
          .thenReturn(OidcParameterNames.ID_TOKEN);
    }

    @Test
    public void oAuth2TokenCustomizerAddsUserInfoToIdTokenWhenSuccess() {
      JwtClaimsSet.Builder claimsBuilder = Mockito.mock(JwtClaimsSet.Builder.class);
      Mockito.when(jwtEncodingContext.getClaims()).thenReturn(claimsBuilder);

      var email = "test@gmail.com";
      var github = "github";
      var user =
          User.builder().email(email).github(github).firstname("first").lastname("last").build();
      Mockito.when(userRepository.findByEmailOrGithub(email, email)).thenReturn(Optional.of(user));
      setupPrincipal(email);
      OAuth2TokenCustomizer<JwtEncodingContext> customizer =
          authorizationServerConfig.oAuth2TokenCustomizer(userRepository);
      customizer.customize(jwtEncodingContext);
      Mockito.verify(jwtEncodingContext.getClaims()).claims(claimsCaptor.capture());

      Map<String, Object> capturedClaims = new HashMap<>();
      claimsCaptor.getValue().accept(capturedClaims);

      Assertions.assertThat(capturedClaims)
          .containsEntry(authorizationServerConfig.EMAIL_KEY_ON_ID_TOKEN, user.getEmail());
      Assertions.assertThat(capturedClaims)
          .containsEntry(authorizationServerConfig.GITHUB_KEY_ON_ID_TOKEN, user.getGithub());
      Assertions.assertThat(capturedClaims)
          .containsEntry(authorizationServerConfig.FIRST_NAME_KEY_ON_ID_TOKEN, user.getFirstname());
      Assertions.assertThat(capturedClaims)
          .containsEntry(authorizationServerConfig.LAST_NAME_KEY_ON_ID_TOKEN, user.getLastname());
      Assertions.assertThat(capturedClaims)
          .containsEntry(authorizationServerConfig.AVATAR_URL_KEY_ON_ID_TOKEN, user.getAvatarUrl());
    }

    @Test
    public void oAuth2TokenCustomizerThrowsExceptionWhenUserNotFound() {
      var email = "test@gmail.com";
      Mockito.when(userRepository.findByEmailOrGithub(email, email)).thenReturn(Optional.empty());
      setupPrincipal(email);
      var customizer = authorizationServerConfig.oAuth2TokenCustomizer(userRepository);
      Assertions.assertThatThrownBy(() -> customizer.customize(jwtEncodingContext))
          .isInstanceOf(UsernameNotFoundException.class)
          .hasMessage("User not found");
    }

    private void setupPrincipal(String principalName) {
      var principal = Mockito.mock(Authentication.class);
      Mockito.when(jwtEncodingContext.getPrincipal()).thenReturn(principal);
      Mockito.when(jwtEncodingContext.getPrincipal().getName()).thenReturn(principalName);
    }
  }
}
