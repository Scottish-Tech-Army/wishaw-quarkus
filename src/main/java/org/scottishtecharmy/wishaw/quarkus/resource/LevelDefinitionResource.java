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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateLevelDefinitionRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateLevelDefinitionRequest;
import org.scottishtecharmy.wishaw.quarkus.model.LevelDefinition;
import org.scottishtecharmy.wishaw.quarkus.repository.LevelDefinitionRepository;

@Path("/manage/level-definitions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class LevelDefinitionResource {

    @Inject
    LevelDefinitionRepository levelDefinitionRepository;

    @GET
    public List<LevelDefinition> list() {
        return levelDefinitionRepository.findAllOrderedByMinXp();
    }

    @GET
    @Path("/{id}")
    public LevelDefinition get(@PathParam("id") UUID id) {
        LevelDefinition level = levelDefinitionRepository.findById(id);
        if (level == null) {
            throw new WebApplicationException("Level definition not found", 404);
        }
        return level;
    }

    @POST
    @Transactional
    public Response create(CreateLevelDefinitionRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new WebApplicationException("Name is required", 400);
        }

        LevelDefinition level = new LevelDefinition();
        level.name = request.name;
        level.minXp = request.minXp;
        level.maxXp = request.maxXp;
        levelDefinitionRepository.persist(level);

        return Response.status(Response.Status.CREATED).entity(level).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public LevelDefinition update(@PathParam("id") UUID id, UpdateLevelDefinitionRequest request) {
        LevelDefinition level = levelDefinitionRepository.findById(id);
        if (level == null) {
            throw new WebApplicationException("Level definition not found", 404);
        }

        if (request.name != null) {
            level.name = request.name;
        }
        if (request.minXp != null) {
            level.minXp = request.minXp;
        }
        if (request.maxXp != null) {
            level.maxXp = request.maxXp;
        }
        levelDefinitionRepository.persist(level);
        return level;
    }
}
