package identify.service;

import identify.model.User;
import identify.reponsitory.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2UserServiceTest {
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock DefaultOAuth2UserService defaultOAuth2UserService;
  @InjectMocks CustomOAuth2UserService customOAuth2UserService;

  @Mock OAuth2UserRequest userRequest;

  @Mock OAuth2User oAuth2User;

  final String email = "test@gmail.com";
  final String github = "github";

  @BeforeEach
  public void setup() {
    Mockito.when(oAuth2User.getAttribute("email")).thenReturn(email);
    Mockito.when(oAuth2User.getAttribute("login")).thenReturn(github);
    Mockito.when(oAuth2User.getAttribute("avatar_url")).thenReturn("avatar");
    Mockito.when(oAuth2User.getAttribute("name")).thenReturn("First Last");
    customOAuth2UserService.defaultOAuth2UserService = defaultOAuth2UserService;
    Mockito.when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);
  }

  @Test
  public void loadUserSaveNewUserWhenUserNotExists() {
    Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn(Mockito.anyString());

    var result = customOAuth2UserService.loadUser(userRequest);

    Mockito.verify(passwordEncoder).encode(Mockito.anyString());
    Mockito.verify(userRepository).save(Mockito.any(User.class));
    Assertions.assertNotNull(result);
  }

  @Test
  public void loadUserUpdateUserWhenUserExistsAndFirstLoginWithGithub() {
    var user = User.builder().email(email).build();
    Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    var result = customOAuth2UserService.loadUser(userRequest);

    user.setGithub(github);
    Mockito.verify(userRepository).save(user);
    Assertions.assertNotNull(result);
  }

  @Test
  public void loadUserThrowsDisabledExceptionWhenUserExistsAndHasBeenDisabled() {
    var userDisabled = User.builder().email(email).enable(false).build();
    Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(userDisabled));

    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> customOAuth2UserService.loadUser(userRequest))
        .isInstanceOf(DisabledException.class)
        .hasMessage("User has been disabled");
  }
}
