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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateCentreRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateCentreRequest;
import org.scottishtecharmy.wishaw.quarkus.model.Centre;
import org.scottishtecharmy.wishaw.quarkus.repository.CentreRepository;

@Path("/manage/centres")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class CentreResource {

    @Inject
    CentreRepository centreRepository;

    @GET
    public List<Centre> list() {
        return centreRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Centre get(@PathParam("id") UUID id) {
        Centre centre = centreRepository.findById(id);
        if (centre == null) {
            throw new WebApplicationException("Centre not found", 404);
        }
        return centre;
    }

    @POST
    @Transactional
    public Response create(CreateCentreRequest request) {
        if (request == null || request.name == null || request.name.isBlank()) {
            throw new WebApplicationException("Name is required", 400);
        }

        Centre centre = new Centre();
        centre.name = request.name;
        centreRepository.persist(centre);

        return Response.status(Response.Status.CREATED).entity(centre).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Centre update(@PathParam("id") UUID id, UpdateCentreRequest request) {
        Centre centre = centreRepository.findById(id);
        if (centre == null) {
            throw new WebApplicationException("Centre not found", 404);
        }

        if (request.name != null) {
            centre.name = request.name;
        }
        if (request.active != null) {
            centre.active = request.active;
        }
        centreRepository.persist(centre);
        return centre;
    }
}
