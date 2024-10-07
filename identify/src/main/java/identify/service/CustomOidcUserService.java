package identify.service;

import common.permission.Role;
import identify.model.User;
import identify.reponsitory.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Custom service for saving and loading {@link User} instances when authorized through an {@link
 * OidcUserService}.
 *
 * <p>This class extends {@link OidcUserService} to provide additional functionality for user
 * management. Specifically, it checks if a user with the given email exists in the database. If the
 * user does not exist, a new user is created with details obtained from the OIDC provider. If the
 * user is found but disabled, a {@link DisabledException} is thrown to indicate that the user is
 * not authorized to log in.
 *
 * @author Duncan Nguyen
 */
@Service
@Slf4j
@AllArgsConstructor
public class CustomOidcUserService extends OidcUserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest)
      throws OAuth2AuthenticationException, DisabledException {
    var oidcUser = super.loadUser(userRequest);
    String email = oidcUser.getEmail();

    String avatarUrl = oidcUser.getPicture();
    String lastName = oidcUser.getFamilyName();
    String firstName = oidcUser.getAttribute("given_name");
    var user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      userRepository.save(
          User.builder()
              .email(email)
              .avatarUrl(avatarUrl)
              .password(passwordEncoder.encode(UUID.randomUUID().toString()))
              .firstname(firstName)
              .lastname(lastName)
              .role(Role.USER)
              .build());
    } else {
      if (!user.isEnabled()) {
        throw new DisabledException("User has been disabled");
      }
    }
    return oidcUser;
  }
}
