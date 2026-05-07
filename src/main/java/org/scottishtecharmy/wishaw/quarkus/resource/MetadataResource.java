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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateMetadataRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateMetadataRequest;
import org.scottishtecharmy.wishaw.quarkus.model.Metadata;
import org.scottishtecharmy.wishaw.quarkus.repository.MetadataRepository;

@Path("/manage/metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class MetadataResource {

    @Inject
    MetadataRepository metadataRepository;

    @GET
    public List<Metadata> list() {
        return metadataRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Metadata get(@PathParam("id") UUID id) {
        Metadata metadata = metadataRepository.findById(id);
        if (metadata == null) {
            throw new WebApplicationException("Metadata not found", 404);
        }
        return metadata;
    }

    @POST
    @Transactional
    public Response create(CreateMetadataRequest request) {
        Metadata metadata = new Metadata();
        metadata.icon = request != null ? request.icon : null;
        metadata.link = request != null ? request.link : null;
        metadataRepository.persist(metadata);

        return Response.status(Response.Status.CREATED).entity(metadata).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Metadata update(@PathParam("id") UUID id, UpdateMetadataRequest request) {
        Metadata metadata = metadataRepository.findById(id);
        if (metadata == null) {
            throw new WebApplicationException("Metadata not found", 404);
        }

        if (request != null) {
            metadata.icon = request.icon;
            metadata.link = request.link;
        }
        metadataRepository.persist(metadata);
        return metadata;
    }
}
