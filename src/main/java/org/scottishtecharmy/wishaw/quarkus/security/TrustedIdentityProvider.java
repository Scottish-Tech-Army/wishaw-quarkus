package org.scottishtecharmy.wishaw.quarkus.security;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TrustedAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;

import java.util.Optional;

/**
 * Handles TrustedAuthenticationRequests produced by CookieAuthMechanism.
 * Runs the blocking DB lookup on a worker thread via context.runBlocking().
 */
@ApplicationScoped
public class TrustedIdentityProvider implements IdentityProvider<TrustedAuthenticationRequest> {

    @Inject
    AppUserRepository appUserRepository;

    @Override
    public Class<TrustedAuthenticationRequest> getRequestType() {
        return TrustedAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TrustedAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            Optional<AppUser> optUser = appUserRepository.findByUsername(request.getPrincipal());
            if (optUser.isEmpty() || !optUser.get().active) {
                throw new AuthenticationFailedException("Invalid or inactive user");
            }

            AppUser user = optUser.get();
            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(user.username))
                    .addRole(user.role.name())
                    .build();
        });
    }
}

