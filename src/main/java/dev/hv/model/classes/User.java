package dev.hv.model.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.hv.model.IId;
import dev.hv.model.decorator.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class User implements IId, IDbItem, UserDetails
{
    @JsonProperty("id")
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;

    @JsonProperty("username")
    @IFieldInfo(fieldName = "username", fieldType = String.class)
    private String _username;

    @JsonProperty("passwordHash")
    @IFieldInfo(fieldName = "password", fieldType = String.class)
    private String _passwordHash;

    @JsonProperty("createdAt")
    @IFieldInfo(fieldName = "createdAt", fieldType = LocalDate.class)
    private LocalDate _createdAt;

    @JsonProperty("updatedAt")
    @IFieldInfo(fieldName = "updatedAt", fieldType = LocalDate.class)
    private LocalDate _updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }
    @Override
    public String getPassword()
    {
        return this._passwordHash;
    }

    public LocalDate getCreatedAt()
    {
        return _createdAt;
    }

    public void setCreatedAt(LocalDate createdAt)
    {
        this._createdAt = createdAt;
    }

    public LocalDate getUpdatedAt()
    {
        return _updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt)
    {
        this._updatedAt = updatedAt;
    }

    public String getUsername()
    {
        return _username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled()
    {
        return UserDetails.super.isEnabled();
    }

    public void setPasswordHash(String passwordHash)
    {
        this._passwordHash = passwordHash;
    }

    public String getPasswordHash()
    {
        return _passwordHash;
    }

    public void setUsername(String username)
    {
        this._username = username;
    }

    @Override
    public UUID getId()
    {
        return this._id;
    }

    @Override
    public void setId(UUID id)
    {
        this._id = id;
    }

    @Override
    public IDbItem dbObjectFactory(Object... args)
    {
        this._id = UUID.fromString((String) args[0]);
        this._username = (String) args[1];
        this._passwordHash = (String) args[2];
        this._createdAt = (LocalDate) args[3];
        if (args[4] != null)
        {
            this._updatedAt = (LocalDate) args[4];
        }

        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("id UUID PRIMARY KEY NOT NULL,");
        strBuilder.append("username VARCHAR(120) NOT NULL,");
        strBuilder.append("passwordHash CHAR(32) NOT NULL,"); // for MD5 Hash
        strBuilder.append("createdAt DATE NOT NULL,");
        strBuilder.append("updatedAt DATE");

        return strBuilder.toString();
    }

    @Override
    public String getSerializedTableName()
    {
        return "user";
    }
}
