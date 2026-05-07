package org.scottishtecharmy.wishaw.quarkus.resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.wishaw.quarkus.dto.BadgesResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.ModuleDto;
import org.scottishtecharmy.wishaw.quarkus.dto.ModuleProgressDto;
import org.scottishtecharmy.wishaw.quarkus.dto.ModulesResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.ProfileResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmitChallengeRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmitChallengeResponse;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.model.Challenge;
import org.scottishtecharmy.wishaw.quarkus.model.ChallengeSubmission;
import org.scottishtecharmy.wishaw.quarkus.model.Module;
import org.scottishtecharmy.wishaw.quarkus.model.SubmissionStatus;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeSubmissionRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ModuleRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;
import org.scottishtecharmy.wishaw.quarkus.service.XpService;

@Path("/me")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"PLAYER", "PARENT", "COACH", "ADMIN"})
@Blocking
public class PlayerResource {

    @Inject
    AuthenticatedUserProvider authenticatedUserProvider;

    @Inject
    XpService xpService;

    @Inject
    ModuleRepository moduleRepository;

    @Inject
    ChallengeRepository challengeRepository;

    @Inject
    ChallengeSubmissionRepository submissionRepository;

    @GET
    @Path("/profile")
    public ProfileResponse profile() {
        AppUser user = authenticatedUserProvider.getCurrentUser();
        ProfileResponse resp = new ProfileResponse();
        resp.userId = user.id;
        resp.displayName = user.username;
        resp.avatarUrl = user.metadata != null ? user.metadata.icon : null;
        return resp;
    }

    @GET
    @Path("/badges")
    public BadgesResponse badges() {
        AppUser user = authenticatedUserProvider.getCurrentUser();
        BadgesResponse resp = new BadgesResponse();
        resp.badges = xpService.getBadgesForUser(user.id);
        return resp;
    }

    @GET
    @Path("/modules")
    public ModulesResponse modules() {
        AppUser user = authenticatedUserProvider.getCurrentUser();
        List<Module> allModules = moduleRepository.listActiveWithAssociations();

        List<ChallengeSubmission> approvedSubmissions =
                submissionRepository.findApprovedByUserId(user.id);
        Set<UUID> approvedChallengeIds = approvedSubmissions.stream()
                .map(s -> s.challenge.id)
                .collect(Collectors.toSet());

        List<ModuleDto> moduleDtos = new ArrayList<>();
        for (Module module : allModules) {
            List<Challenge> challenges = challengeRepository.findByModuleId(module.id);

            ModuleProgressDto progress = new ModuleProgressDto();
            progress.total = challenges.size();
            progress.approved = (int) challenges.stream()
                    .filter(c -> approvedChallengeIds.contains(c.id))
                    .count();

            ModuleDto dto = new ModuleDto();
            dto.moduleId = module.id;
            dto.name = module.displayName;
            dto.game = module.game.displayName;
            dto.progress = progress;
            moduleDtos.add(dto);
        }

        ModulesResponse resp = new ModulesResponse();
        resp.modules = moduleDtos;
        return resp;
    }

    @POST
    @Path("/challenges/{challengeId}/submit")
    @Transactional
    public SubmitChallengeResponse submitChallenge(
            @PathParam("challengeId") UUID challengeId,
            SubmitChallengeRequest request) {

        AppUser user = authenticatedUserProvider.getCurrentUser();

        Challenge challenge = challengeRepository.findById(challengeId);
        if (challenge == null) {
            throw new WebApplicationException("Challenge not found", 404);
        }

        ChallengeSubmission submission = new ChallengeSubmission();
        submission.challenge = challenge;
        submission.submittedBy = user;
        submission.noteText = request != null ? request.noteText : null;
        submission.status = SubmissionStatus.SUBMITTED;
        submission.submittedTs = Instant.now();
        submission.persist();

        SubmitChallengeResponse resp = new SubmitChallengeResponse();
        resp.submissionId = submission.id;
        resp.status = submission.status.name();
        return resp;
    }
}
