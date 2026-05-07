package org.scottishtecharmy.wishaw.quarkus.resource;

import java.util.List;
import java.util.UUID;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.wishaw.quarkus.dto.CreateUserRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateUserRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UserDto;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.model.UserRole;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.CentreRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.MetadataRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;
import org.scottishtecharmy.wishaw.quarkus.security.PasswordUtil;

@Path("/manage/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class UserResource {

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    CentreRepository centreRepository;

    @Inject
    MetadataRepository metadataRepository;

    @Inject
    AuthenticatedUserProvider authenticatedUserProvider;

    private UserDto toDto(AppUser user) {
        UserDto dto = new UserDto();
        dto.userId = user.id;
        dto.centreId = user.centre.id;
        dto.username = user.username;
        dto.displayName = user.username;
        dto.role = user.role.name();
        dto.active = user.active;
        return dto;
    }

    @GET
    public List<UserDto> list() {
        return appUserRepository.listAllWithCentre()
                .stream().map(this::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public UserDto get(@PathParam("id") UUID id) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        AppUser user = appUserRepository.findById(id);
        if (user == null || !user.centre.id.equals(admin.centre.id)) {
            throw new WebApplicationException("User not found", 404);
        }
        return toDto(user);
    }

    @POST
    @Transactional
    public Response create(CreateUserRequest request) {
        if (request == null || request.username == null || request.username.isBlank()) {
            throw new WebApplicationException("Username is required", 400);
        }
        if (request.password == null || request.password.isBlank()) {
            throw new WebApplicationException("Password is required", 400);
        }
        if (request.role == null || request.role.isBlank()) {
            throw new WebApplicationException("Role is required", 400);
        }

        AppUser admin = authenticatedUserProvider.getCurrentUser();

        AppUser user = new AppUser();
        user.username = request.username;
        user.passwordHash = PasswordUtil.hashPassword(request.password);
        user.role = UserRole.valueOf(request.role);
        user.centre = request.centreId != null
                ? centreRepository.findById(request.centreId)
                : admin.centre;

        if (user.centre == null) {
            throw new WebApplicationException("Centre not found", 400);
        }
        if (request.metadataId != null) {
            user.metadata = metadataRepository.findById(request.metadataId);
            if (user.metadata == null) throw new WebApplicationException("Metadata not found", 400);
        }
        if (request.parentId != null) {
            user.parent = appUserRepository.findById(request.parentId);
            if (user.parent == null) throw new WebApplicationException("Parent user not found", 400);
        }

        appUserRepository.persist(user);
        return Response.status(Response.Status.CREATED).entity(toDto(user)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public UserDto update(@PathParam("id") UUID id, UpdateUserRequest request) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        AppUser user = appUserRepository.findById(id);
        if (user == null || !user.centre.id.equals(admin.centre.id)) {
            throw new WebApplicationException("User not found", 404);
        }
        if (request.role != null) user.role = UserRole.valueOf(request.role);
        if (request.active != null) user.active = request.active;
        if (request.password != null && !request.password.isBlank()) {
            user.passwordHash = PasswordUtil.hashPassword(request.password);
        }
        if (request.metadataId != null) {
            user.metadata = metadataRepository.findById(request.metadataId);
            if (user.metadata == null) throw new WebApplicationException("Metadata not found", 400);
        }
        if (request.parentId != null) {
            user.parent = appUserRepository.findById(request.parentId);
            if (user.parent == null) throw new WebApplicationException("Parent user not found", 400);
        }
        appUserRepository.persist(user);
        return toDto(user);
    }
}
