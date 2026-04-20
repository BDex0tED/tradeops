package com.tradeops.service.tenant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("tenantAuth")
@RequiredArgsConstructor
public class TenantSecurityService {
    private final HttpServletRequest request;

    public boolean canAccess(Long requestedTraderId){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            return false;
        }

        if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))){
            return true;
        }

        Long traderId = request.getAttribute("X-Current-Trader-Id") != null ? (Long) request.getAttribute("X-Current-Trader-Id") : null;
        return traderId != null && traderId.equals(requestedTraderId);
    }
}
