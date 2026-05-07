package org.scottishtecharmy.wishaw.quarkus.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.wishaw.quarkus.dto.LeaderboardResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.LeaderboardRow;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.model.UserRole;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;
import org.scottishtecharmy.wishaw.quarkus.service.XpService;

@Path("/leaderboards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"PLAYER", "PARENT", "COACH", "ADMIN"})
@Blocking
public class LeaderboardResource {

    @Inject
    AuthenticatedUserProvider authenticatedUserProvider;

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    XpService xpService;

    @GET
    @Path("/centre")
    public LeaderboardResponse centreLeaderboard() {
        AppUser currentUser = authenticatedUserProvider.getCurrentUser();
        List<AppUser> users = appUserRepository.findByCentreId(currentUser.centre.id);

        return buildLeaderboard(users, false);
    }

    @GET
    @Path("/global")
    public LeaderboardResponse globalLeaderboard() {
        List<AppUser> users = appUserRepository.list("role", UserRole.PLAYER);

        return buildLeaderboard(users, true);
    }

    private LeaderboardResponse buildLeaderboard(List<AppUser> users, boolean includeCentreName) {
        List<LeaderboardRow> rows = new ArrayList<>();

        for (AppUser user : users) {
            if (user.role != UserRole.PLAYER || !user.active) {
                continue;
            }

            LeaderboardRow row = new LeaderboardRow();
            row.username = user.username;
            row.displayName = user.username;
            row.totalXp = xpService.getTotalXpForUser(user.id);
            if (includeCentreName) {
                row.centreName = user.centre.name;
            }
            rows.add(row);
        }

        // Sort descending by totalXp
        rows.sort(Comparator.comparingInt((LeaderboardRow r) -> r.totalXp).reversed());

        // Assign ranks
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).rank = i + 1;
        }

        LeaderboardResponse resp = new LeaderboardResponse();
        resp.rows = rows;
        return resp;
    }
}
