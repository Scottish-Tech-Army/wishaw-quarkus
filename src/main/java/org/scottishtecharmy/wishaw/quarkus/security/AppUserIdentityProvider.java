package org.scottishtecharmy.wishaw.quarkus.security;

import java.util.Optional;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;

/**
 * Custom IdentityProvider that authenticates users against the app_user table
 * with bcrypt password verification.
 */
@ApplicationScoped
public class AppUserIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Inject
    AppUserRepository appUserRepository;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                               AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            String username = request.getUsername();
            String password = new String(request.getPassword().getPassword());

            Optional<AppUser> optUser = appUserRepository.findByUsername(username);
            if (optUser.isEmpty()) {
                throw new AuthenticationFailedException("Invalid credentials");
            }

            AppUser user = optUser.get();
            if (!user.active) {
                throw new AuthenticationFailedException("Account is disabled");
            }

            if (!BcryptUtil.matches(password, user.passwordHash)) {
                throw new AuthenticationFailedException("Invalid credentials");
            }

            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(new QuarkusPrincipal(user.username))
                    .addRole(user.role.name())
                    .build();
        });
    }
}

