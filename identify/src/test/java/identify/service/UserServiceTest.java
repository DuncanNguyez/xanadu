package identify.service;

import identify.model.User;
import identify.reponsitory.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  @Mock UserRepository userRepository;

  @InjectMocks UserService userService;

  @Test
  public void loadUserByUsernameReturnUserDetailWhenUserExists() {
    var email = "test@gmail.com";
    var user = User.builder().email(email).build();
    Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    var userDetail = userService.loadUserByUsername(email);

    Mockito.verify(userRepository).findByEmail(email);
    Assertions.assertNotNull(userDetail);
    Assertions.assertEquals(email, userDetail.getUsername());
  }

  @Test
  public void loadUserByUsernameThrowsExceptionWhenUserDoseNotExists() {
    var email = "test@gmail.com";
    Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        UsernameNotFoundException.class,
        () -> {
          userService.loadUserByUsername(email);
        },
        "User not found");
    Mockito.verify(userRepository).findByEmail(email);
  }
}
