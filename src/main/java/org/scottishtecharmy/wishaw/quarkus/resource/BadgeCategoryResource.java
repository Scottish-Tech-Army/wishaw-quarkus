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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateBadgeCategoryRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateBadgeCategoryRequest;
import org.scottishtecharmy.wishaw.quarkus.model.BadgeCategory;
import org.scottishtecharmy.wishaw.quarkus.repository.BadgeCategoryRepository;

@Path("/manage/badge-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class BadgeCategoryResource {

    @Inject
    BadgeCategoryRepository badgeCategoryRepository;

    @GET
    public List<BadgeCategory> list() {
        return badgeCategoryRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public BadgeCategory get(@PathParam("id") UUID id) {
        BadgeCategory category = badgeCategoryRepository.findById(id);
        if (category == null) {
            throw new WebApplicationException("Badge category not found", 404);
        }
        return category;
    }

    @POST
    @Transactional
    public Response create(CreateBadgeCategoryRequest request) {
        if (request == null || request.displayName == null || request.displayName.isBlank()) {
            throw new WebApplicationException("Display name is required", 400);
        }

        BadgeCategory category = new BadgeCategory();
        category.displayName = request.displayName;
        category.description = request.description;
        badgeCategoryRepository.persist(category);

        return Response.status(Response.Status.CREATED).entity(category).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public BadgeCategory update(@PathParam("id") UUID id, UpdateBadgeCategoryRequest request) {
        BadgeCategory category = badgeCategoryRepository.findById(id);
        if (category == null) {
            throw new WebApplicationException("Badge category not found", 404);
        }

        if (request.displayName != null) {
            category.displayName = request.displayName;
        }
        if (request.description != null) {
            category.description = request.description;
        }
        badgeCategoryRepository.persist(category);
        return category;
    }
}
