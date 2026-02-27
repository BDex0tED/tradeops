package com.tradeops.model.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public enum UserRolePermission {
    ROLE_SUPER_ADMIN(Set.of("*")),
    ROLE_DISPATCHER(Set.of("orders:all:read", "dispatch:manage", "audit:read")),
    ROLE_COURIER(Set.of("orders:own:read", "orders:own:write", "audit:read")),
    ROLE_TRADER_ADMIN(Set.of("orders:own:read", "orders:own:write", "catalog:read", "theme:write")),
    ROLE_CATALOG_MANAGER(Set.of("catalog:write", "catalog:read", "inventory:read"));

    private final Set<String> scopes;

    UserRolePermission(Set<String> scopes) {
        this.scopes = scopes;
    }

    public static Set<String> getScopesByRoleName(String roleName) {
        return Arrays.stream(values())
                .filter(mapping -> mapping.name().equals(roleName))
                .findFirst()
                .map(mapping -> mapping.scopes)
                .orElse(Collections.emptySet());
    }
}
