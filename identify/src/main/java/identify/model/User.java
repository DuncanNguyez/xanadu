package identify.model;

import jakarta.persistence.*;
import java.util.Collection;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "`user`")
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstname;
  private String lastname;
  private String email;
  private String password;
  private String github;
  private String avatarUrl;

  @Builder.Default private Boolean enable = true;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.USER;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public boolean isEnabled() {
    return this.enable;
  }
}
