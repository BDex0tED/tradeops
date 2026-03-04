package com.tradeops.service.audit;

import com.tradeops.model.entity.ActorType;
import com.tradeops.model.entity.UserEntity;
import com.tradeops.repo.UserEntityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditIdentityResolver {

    private final UserEntityRepo userEntityRepo;

    public AuditIdentity getCurrentIdentity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return new AuditIdentity(ActorType.SYSTEM, 0L);
        }

        String username = auth.getName();
        UserEntity user = userEntityRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));

        ActorType actorType = determineActorType(user);
        return new AuditIdentity(actorType, user.getId());
    }

    private ActorType determineActorType(UserEntity user) {
        String roleStr = user.getRoles().stream()
                .findFirst()
                .map(role -> role.getName())
                .orElse("UNKNOWN");

        if (roleStr.startsWith("ROLE_TRADER"))
            return ActorType.TRADER;
        if (roleStr.contains("COURIER"))
            return ActorType.COURIER;
        if (roleStr.contains("SUPER_ADMIN"))
            return ActorType.SYSTEM;

        return ActorType.COMPANY;
    }

    public record AuditIdentity(ActorType actorType, Long actorId) {
    }
}
