package org.scottishtecharmy.wishaw.quarkus.resource;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.wishaw.quarkus.dto.LoginRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.LoginResponse;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;
import org.scottishtecharmy.wishaw.quarkus.security.SecurityConstants;
import org.scottishtecharmy.wishaw.quarkus.security.SessionCookieUtil;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AuthResource {

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    AuthenticatedUserProvider authenticatedUserProvider;

    @Inject
    SecurityIdentity securityIdentity;

    private static String buildSetCookieHeader(String name, String value, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append(";");
        sb.append(" Path=/;");
        sb.append(" HttpOnly;");
        sb.append(" SameSite=Strict;");
        sb.append(" Max-Age=").append(maxAge);
        return sb.toString();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest request) {
        if (request == null || request.username == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Username and password are required\"}")
                    .build();
        }

        var optUser = appUserRepository.findByUsername(request.username);
        if (optUser.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid credentials\"}")
                    .build();
        }

        AppUser user = optUser.get();
        if (!user.active) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Account is disabled\"}")
                    .build();
        }

        if (!BcryptUtil.matches(request.password, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid credentials\"}")
                    .build();
        }

        String cookieHeader = buildSetCookieHeader(
                SecurityConstants.SESSION_COOKIE_NAME,
                SessionCookieUtil.encode(user.username),
                86400);

        LoginResponse resp = new LoginResponse();
        resp.userId = user.id;
        resp.username = user.username;
        resp.role = user.role.name();
        resp.centreId = user.centre.id;
        resp.displayName = user.username;

        return Response.ok(resp)
                .header("Set-Cookie", cookieHeader)
                .build();
    }

    @POST
    @Path("/logout")
    @RolesAllowed({"PLAYER", "PARENT", "COACH", "ADMIN"})
    public Response logout() {
        String cookieHeader = buildSetCookieHeader(
                SecurityConstants.SESSION_COOKIE_NAME, "", 0);

        return Response.ok("{\"message\":\"Logged out\"}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("Set-Cookie", cookieHeader)
                .build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"PLAYER", "PARENT", "COACH", "ADMIN"})
    public LoginResponse me() {
        AppUser user = authenticatedUserProvider.getCurrentUser();
        LoginResponse resp = new LoginResponse();
        resp.userId = user.id;
        resp.username = user.username;
        resp.role = user.role.name();
        resp.centreId = user.centre.id;
        resp.displayName = user.username;
        return resp;
    }
}
