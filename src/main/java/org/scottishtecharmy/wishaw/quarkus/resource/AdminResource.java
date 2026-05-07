package org.scottishtecharmy.wishaw.quarkus.resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.wishaw.quarkus.dto.ReviewRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmissionDto;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmissionsResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmitChallengeResponse;
import org.scottishtecharmy.wishaw.quarkus.dto.UserDto;
import org.scottishtecharmy.wishaw.quarkus.dto.UsersResponse;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.model.ChallengeSubmission;
import org.scottishtecharmy.wishaw.quarkus.model.SubmissionStatus;
import org.scottishtecharmy.wishaw.quarkus.repository.AppUserRepository;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeSubmissionRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"COACH", "ADMIN"})
@Blocking
public class AdminResource {

    @Inject
    AuthenticatedUserProvider authenticatedUserProvider;

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    ChallengeSubmissionRepository submissionRepository;

    @GET
    @Path("/centre/users")
    public UsersResponse centreUsers() {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        List<AppUser> users = appUserRepository.findByCentreId(admin.centre.id);

        List<UserDto> userDtos = new ArrayList<>();
        for (AppUser user : users) {
            UserDto dto = new UserDto();
            dto.userId = user.id;
            dto.username = user.username;
            dto.displayName = user.username;
            dto.role = user.role.name();
            dto.active = user.active;
            userDtos.add(dto);
        }

        UsersResponse resp = new UsersResponse();
        resp.users = userDtos;
        return resp;
    }

    @GET
    @Path("/centre/submissions")
    public SubmissionsResponse centreSubmissions(
            @QueryParam("status") @DefaultValue("SUBMITTED") String statusParam) {

        AppUser admin = authenticatedUserProvider.getCurrentUser();
        SubmissionStatus status = SubmissionStatus.valueOf(statusParam);

        List<ChallengeSubmission> submissions =
                submissionRepository.findByStatusAndCentreId(status, admin.centre.id);

        List<SubmissionDto> dtos = new ArrayList<>();
        for (ChallengeSubmission sub : submissions) {
            SubmissionDto dto = new SubmissionDto();
            dto.submissionId = sub.id;
            dto.challengeId = sub.challenge.id;
            dto.challengeName = sub.challenge.displayName;
            dto.username = sub.submittedBy.username;
            dto.displayName = sub.submittedBy.username;
            dto.noteText = sub.noteText;
            dto.submittedAt = sub.submittedTs.toString();
            dtos.add(dto);
        }

        SubmissionsResponse resp = new SubmissionsResponse();
        resp.submissions = dtos;
        return resp;
    }

    @POST
    @Path("/submissions/{submissionId}/approve")
    @Transactional
    public SubmitChallengeResponse approve(
            @PathParam("submissionId") UUID submissionId,
            ReviewRequest request) {

        return reviewSubmission(submissionId, SubmissionStatus.APPROVED,
                request != null ? request.reviewerComment : null);
    }

    @POST
    @Path("/submissions/{submissionId}/reject")
    @Transactional
    public SubmitChallengeResponse reject(
            @PathParam("submissionId") UUID submissionId,
            ReviewRequest request) {

        if (request == null || request.reviewerComment == null || request.reviewerComment.isBlank()) {
            throw new WebApplicationException("Reviewer comment is required for rejection", 400);
        }

        return reviewSubmission(submissionId, SubmissionStatus.REJECTED, request.reviewerComment);
    }

    private SubmitChallengeResponse reviewSubmission(UUID submissionId, SubmissionStatus newStatus,
                                                      String reviewerComment) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();

        ChallengeSubmission submission = submissionRepository.findById(submissionId);
        if (submission == null) {
            throw new WebApplicationException("Submission not found", 404);
        }

        // Centre isolation: admin can only review submissions from their own centre
        if (!submission.submittedBy.centre.id.equals(admin.centre.id)) {
            throw new WebApplicationException("Submission not found", 404);
        }

        if (submission.status != SubmissionStatus.SUBMITTED) {
            throw new WebApplicationException("Submission has already been reviewed", 400);
        }

        submission.status = newStatus;
        submission.reviewedTs = Instant.now();
        submission.reviewedBy = admin;
        submission.reviewerComment = reviewerComment;
        submission.persist();

        SubmitChallengeResponse resp = new SubmitChallengeResponse();
        resp.submissionId = submission.id;
        resp.status = submission.status.name();
        return resp;
    }
}
