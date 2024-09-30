package identify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Duncan Nguyen
 * @since 0.0.1
 */
@Controller
public class LoginController {

  @GetMapping("/login")
  public String login() {
    return "login";
  }
}
