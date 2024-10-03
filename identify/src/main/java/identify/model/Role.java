package identify.model;

import static identify.model.Permission.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RequiredArgsConstructor
public enum Role implements IRole {
  USER(Collections.emptySet(), Collections.emptySet()),
  MANAGER(Set.of(USER), Set.of(MANAGER_CREATE, MANAGER_UPDATE, MANAGER_DELETE, MANAGER_READ)),
  ADMIN(Set.of(USER, MANAGER), Set.of(ADMIN_READ, ADMIN_DELETE, ADMIN_CREATE, ADMIN_UPDATE));
  private final Set<Role> childRoles;
  private final Set<Permission> permissions;

  @Override
  public Set<Permission> getPermissions() {
    Set<Permission> allPermissions = new HashSet<>(this.permissions);
    childRoles.forEach(r -> allPermissions.addAll(r.getPermissions()));
    return allPermissions;
  }

  @Override
  public List<SimpleGrantedAuthority> getAuthorities() {
    var authorities =
        getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
            .collect(Collectors.toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    return authorities;
  }
}
