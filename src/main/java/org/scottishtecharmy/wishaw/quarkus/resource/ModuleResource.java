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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateModuleRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.ManageModuleDto;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateModuleRequest;
import org.scottishtecharmy.wishaw.quarkus.model.Game;
import org.scottishtecharmy.wishaw.quarkus.model.Metadata;
import org.scottishtecharmy.wishaw.quarkus.model.Module;
import org.scottishtecharmy.wishaw.quarkus.repository.GameRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.MetadataRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ModuleRepository;

@Path("/manage/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class ModuleResource {

    @Inject
    ModuleRepository moduleRepository;

    @Inject
    GameRepository gameRepository;

    @Inject
    MetadataRepository metadataRepository;

    private ManageModuleDto toDto(Module module) {
        ManageModuleDto dto = new ManageModuleDto();
        dto.moduleId = module.id;
        dto.gameId = module.game.id;
        dto.displayName = module.displayName;
        dto.description = module.description;
        dto.active = module.active;
        return dto;
    }

    @GET
    public List<ManageModuleDto> list() {
        return moduleRepository.listAllWithAssociations().stream().map(this::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public ManageModuleDto get(@PathParam("id") UUID id) {
        Module module = moduleRepository.findById(id);
        if (module == null) {
            throw new WebApplicationException("Module not found", 404);
        }
        return toDto(module);
    }

    @POST
    @Transactional
    public Response create(CreateModuleRequest request) {
        if (request == null || request.displayName == null || request.displayName.isBlank()) {
            throw new WebApplicationException("Display name is required", 400);
        }
        if (request.gameId == null) {
            throw new WebApplicationException("Game ID is required", 400);
        }

        Game game = gameRepository.findById(request.gameId);
        if (game == null) {
            throw new WebApplicationException("Game not found", 400);
        }

        Module module = new Module();
        module.displayName = request.displayName;
        module.description = request.description;
        module.game = game;

        if (request.metadataId != null) {
            Metadata metadata = metadataRepository.findById(request.metadataId);
            if (metadata == null) {
                throw new WebApplicationException("Metadata not found", 400);
            }
            module.metadata = metadata;
        }

        moduleRepository.persist(module);
        return Response.status(Response.Status.CREATED).entity(toDto(module)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public ManageModuleDto update(@PathParam("id") UUID id, UpdateModuleRequest request) {
        Module module = moduleRepository.findById(id);
        if (module == null) {
            throw new WebApplicationException("Module not found", 404);
        }

        if (request.displayName != null) {
            module.displayName = request.displayName;
        }
        if (request.description != null) {
            module.description = request.description;
        }
        if (request.active != null) {
            module.active = request.active;
        }
        if (request.gameId != null) {
            Game game = gameRepository.findById(request.gameId);
            if (game == null) {
                throw new WebApplicationException("Game not found", 400);
            }
            module.game = game;
        }
        if (request.metadataId != null) {
            Metadata metadata = metadataRepository.findById(request.metadataId);
            if (metadata == null) {
                throw new WebApplicationException("Metadata not found", 400);
            }
            module.metadata = metadata;
        }

        moduleRepository.persist(module);
        return toDto(module);
    }
}
