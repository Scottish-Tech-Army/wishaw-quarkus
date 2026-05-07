package org.scottishtecharmy.wishaw.quarkus.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TrustedAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class CookieAuthMechanism implements HttpAuthenticationMechanism {

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {
        io.vertx.core.http.Cookie cookie = context.request().getCookie(SecurityConstants.SESSION_COOKIE_NAME);
        if (cookie == null) {
            return Uni.createFrom().nullItem();
        }

        // JAX-RS NewCookie wraps the value in quotes (RFC 2109); strip them if present
        String rawValue = cookie.getValue();
        if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            rawValue = rawValue.substring(1, rawValue.length() - 1);
        }

        String username = SessionCookieUtil.decode(rawValue);
        if (username == null) {
            return Uni.createFrom().nullItem();
        }

        return identityProviderManager.authenticate(new TrustedAuthenticationRequest(username));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TrustedAuthenticationRequest.class);
    }
}
