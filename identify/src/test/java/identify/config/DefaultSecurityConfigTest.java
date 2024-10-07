package identify.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
public class DefaultSecurityConfigTest {
  private final DefaultSecurityConfig defaultSecurityConfig = new DefaultSecurityConfig();
  @Mock private HttpServletRequest httpServletRequest;
  @Mock private HttpServletResponse httpServletResponse;

  @Test
  public void
      redirectWithInternalServerErrorMessageWhenExceptionInstanceOfInternalAuthenticationServerException()
          throws ServletException, IOException {
    InternalAuthenticationServiceException exception =
        new InternalAuthenticationServiceException("error");
    defaultSecurityConfig.authenticationFailureHandler.onAuthenticationFailure(
        httpServletRequest, httpServletResponse, exception);
    Mockito.verify(httpServletResponse).sendRedirect("/login?error=Internal Server Error");
  }

  @Test
  public void redirectWithDetailErrorMessageWhitRemainingException()
      throws ServletException, IOException {
    var detailMessage = "error message";
    AuthenticationException exception = Mockito.mock(AuthenticationException.class);
    Mockito.when(exception.getMessage()).thenReturn(detailMessage);
    defaultSecurityConfig.authenticationFailureHandler.onAuthenticationFailure(
        httpServletRequest, httpServletResponse, exception);
    String errorMessage = exception.getMessage();
    String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    Mockito.verify(httpServletResponse).sendRedirect("/login?error=" + encodedMessage);
  }
}
