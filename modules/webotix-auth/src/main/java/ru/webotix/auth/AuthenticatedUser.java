package ru.webotix.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.security.Principal;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class AuthenticatedUser implements Principal {

    private final String name;
    private final Set<String> roles;

    public AuthenticatedUser(String name, String roles) {
        this.name = name;
        this.roles =
                StringUtils.isEmpty(roles)
                        ? ImmutableSet.of()
                        : ImmutableSet.copyOf(Splitter.on(",").split(roles));
    }

    @Override
    public String getName() {
        return name;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AuthenticatedUser other = (AuthenticatedUser) obj;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (roles == null) {
            return other.roles == null;
        } else return roles.equals(other.roles);
    }
}