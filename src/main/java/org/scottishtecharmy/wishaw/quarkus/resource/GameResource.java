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
import org.scottishtecharmy.wishaw.quarkus.dto.CreateGameRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.UpdateGameRequest;
import org.scottishtecharmy.wishaw.quarkus.model.Game;
import org.scottishtecharmy.wishaw.quarkus.repository.GameRepository;

@Path("/manage/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class GameResource {

    @Inject
    GameRepository gameRepository;

    @GET
    public List<Game> list() {
        return gameRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Game get(@PathParam("id") UUID id) {
        Game game = gameRepository.findById(id);
        if (game == null) {
            throw new WebApplicationException("Game not found", 404);
        }
        return game;
    }

    @POST
    @Transactional
    public Response create(CreateGameRequest request) {
        if (request == null || request.displayName == null || request.displayName.isBlank()) {
            throw new WebApplicationException("Display name is required", 400);
        }

        Game game = new Game();
        game.displayName = request.displayName;
        gameRepository.persist(game);

        return Response.status(Response.Status.CREATED).entity(game).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Game update(@PathParam("id") UUID id, UpdateGameRequest request) {
        Game game = gameRepository.findById(id);
        if (game == null) {
            throw new WebApplicationException("Game not found", 404);
        }

        if (request.displayName != null) {
            game.displayName = request.displayName;
        }
        if (request.active != null) {
            game.active = request.active;
        }
        gameRepository.persist(game);
        return game;
    }
}
