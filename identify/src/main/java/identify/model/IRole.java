package identify.model;

import java.util.List;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public interface IRole {
  public Set<Permission> getPermissions();

  public List<SimpleGrantedAuthority> getAuthorities();
}
