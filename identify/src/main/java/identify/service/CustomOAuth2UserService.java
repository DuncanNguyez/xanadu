package identify.service;

import identify.model.Role;
import identify.model.User;
import identify.reponsitory.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Custom service for loading {@link OAuth2User} and managing {@link User} instances when
 * authenticated via OAuth2.
 *
 * <p>This class implements {@link OAuth2UserService} to handle user creation and updates based on
 * data retrieved from an OAuth2 provider (e.g., GitHub). If a user with the given email does not
 * exist in the database, a new user is created. If the user exists but is disabled, a {@link
 * DisabledException} is thrown. Additionally, if the user exists but their GitHub username is not
 * set, it is updated in the database.
 *
 * @author Duncan Nguyen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest)
      throws OAuth2AuthenticationException, DisabledException {
    DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

    String email = oAuth2User.getAttribute("email");
    String github = oAuth2User.getAttribute("login");
    String avatarUrl = oAuth2User.getAttribute("avatar_url");
    String name = oAuth2User.getAttribute("name");
    assert name != null;
    String firstName = name.replaceFirst(" .*", "");
    String lastName = name.replaceFirst(".+ ", "");
    var user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      userRepository.save(
          User.builder()
              .email(email)
              .github(github)
              .avatarUrl(avatarUrl)
              .password(passwordEncoder.encode(UUID.randomUUID().toString()))
              .firstname(firstName)
              .lastname(lastName)
              .role(Role.USER)
              .build());
    } else {
      if (!user.isEnabled()) {
        throw new DisabledException("User has been disable");
      } else if (user.getGithub() == null) {
        user.setGithub(github);
        userRepository.save(user);
      }
    }
    return oAuth2User;
  }
}
