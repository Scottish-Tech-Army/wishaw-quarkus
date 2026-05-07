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
import org.scottishtecharmy.wishaw.quarkus.dto.ChallengeDto;
import org.scottishtecharmy.wishaw.quarkus.dto.CreateChallengeRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateChallengeRequest;
import org.scottishtecharmy.wishaw.quarkus.model.BadgeCategory;
import org.scottishtecharmy.wishaw.quarkus.model.Challenge;
import org.scottishtecharmy.wishaw.quarkus.model.Metadata;
import org.scottishtecharmy.wishaw.quarkus.model.Module;
import org.scottishtecharmy.wishaw.quarkus.repository.BadgeCategoryRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.MetadataRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ModuleRepository;

@Path("/manage/challenges")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class ChallengeResource {

    @Inject ChallengeRepository challengeRepository;
    @Inject ModuleRepository moduleRepository;
    @Inject BadgeCategoryRepository badgeCategoryRepository;
    @Inject MetadataRepository metadataRepository;

    private ChallengeDto toDto(Challenge challenge) {
        ChallengeDto dto = new ChallengeDto();
        dto.challengeId = challenge.id;
        dto.moduleId = challenge.module.id;
        dto.badgeCategoryId = challenge.badgeCategory.id;
        dto.displayName = challenge.displayName;
        dto.description = challenge.description;
        dto.xpValue = challenge.xpValue;
        return dto;
    }

    @GET
    public List<ChallengeDto> list() {
        return challengeRepository.listAllWithAssociations().stream().map(this::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public ChallengeDto get(@PathParam("id") UUID id) {
        Challenge challenge = challengeRepository.findById(id);
        if (challenge == null) throw new WebApplicationException("Challenge not found", 404);
        return toDto(challenge);
    }

    @POST
    @Transactional
    public Response create(CreateChallengeRequest request) {
        if (request == null || request.displayName == null || request.displayName.isBlank())
            throw new WebApplicationException("Display name is required", 400);
        if (request.moduleId == null)
            throw new WebApplicationException("Module ID is required", 400);
        if (request.badgeCategoryId == null)
            throw new WebApplicationException("Badge category ID is required", 400);

        Module module = moduleRepository.findById(request.moduleId);
        if (module == null) throw new WebApplicationException("Module not found", 400);

        BadgeCategory badgeCategory = badgeCategoryRepository.findById(request.badgeCategoryId);
        if (badgeCategory == null) throw new WebApplicationException("Badge category not found", 400);

        Challenge challenge = new Challenge();
        challenge.displayName = request.displayName;
        challenge.description = request.description;
        challenge.module = module;
        challenge.badgeCategory = badgeCategory;
        challenge.xpValue = request.xpValue;

        if (request.metadataId != null) {
            Metadata metadata = metadataRepository.findById(request.metadataId);
            if (metadata == null) throw new WebApplicationException("Metadata not found", 400);
            challenge.metadata = metadata;
        }

        challengeRepository.persist(challenge);
        return Response.status(Response.Status.CREATED).entity(toDto(challenge)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public ChallengeDto update(@PathParam("id") UUID id, UpdateChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(id);
        if (challenge == null) throw new WebApplicationException("Challenge not found", 404);

        if (request.displayName != null) challenge.displayName = request.displayName;
        if (request.description != null) challenge.description = request.description;
        if (request.xpValue != null) challenge.xpValue = request.xpValue;
        if (request.badgeCategoryId != null) {
            BadgeCategory badgeCategory = badgeCategoryRepository.findById(request.badgeCategoryId);
            if (badgeCategory == null) throw new WebApplicationException("Badge category not found", 400);
            challenge.badgeCategory = badgeCategory;
        }
        if (request.metadataId != null) {
            Metadata metadata = metadataRepository.findById(request.metadataId);
            if (metadata == null) throw new WebApplicationException("Metadata not found", 400);
            challenge.metadata = metadata;
        }

        challengeRepository.persist(challenge);
        return toDto(challenge);
    }
}
