package dev.hv.model.classes.Authentification;

import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;

import java.util.List;
import java.util.UUID;

public class AuthUserDto
{
    private UUID id;
    private String username;
    private String password;
    private UserRoles role;
    private List<UserPermissions> permissions;

    public AuthUserDto()
    {
    }

    public AuthUserDto(AuthUser user)
    {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.permissions = user.getPermissions();
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; }

    public UserRoles getRole() { return role; }

    public void setRole(UserRoles role) { this.role = role; }

    public List<UserPermissions> getPermissions() { return permissions; }

    public void setPermissions(List<UserPermissions> permissions) { this.permissions = permissions; }
}
