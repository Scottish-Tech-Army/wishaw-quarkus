package org.scottishtecharmy.wishaw.quarkus.security;

import java.security.Principal;
import java.util.UUID;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;

/**
 * Helper to resolve the currently authenticated AppUser from the SecurityIdentity.
 */
@ApplicationScoped
public class AuthenticatedUserProvider {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AppUserRepository appUserRepository;

    public AppUser getCurrentUser() {
        Principal principal = securityIdentity.getPrincipal();
        return appUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new jakarta.ws.rs.WebApplicationException("User not found", 401));
    }

    public UUID getCurrentCentreId() {
        return getCurrentUser().centre.id;
    }
}

