package org.scottishtecharmy.wishaw.quarkus.resource;

import java.util.List;
import java.util.UUID;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.scottishtecharmy.wishaw.quarkus.dto.ReviewRequest;
import org.scottishtecharmy.wishaw.quarkus.dto.SubmissionDto;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;
import org.scottishtecharmy.wishaw.quarkus.model.ChallengeSubmission;
import org.scottishtecharmy.wishaw.quarkus.model.SubmissionStatus;
import org.scottishtecharmy.wishaw.quarkus.repository.ChallengeSubmissionRepository;
import org.scottishtecharmy.wishaw.quarkus.security.AuthenticatedUserProvider;

@Path("/manage/submissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Blocking
public class ChallengeSubmissionResource {

    @Inject ChallengeSubmissionRepository submissionRepository;
    @Inject AuthenticatedUserProvider authenticatedUserProvider;

    private SubmissionDto toDto(ChallengeSubmission sub) {
        SubmissionDto dto = new SubmissionDto();
        dto.submissionId = sub.id;
        dto.challengeId = sub.challenge.id;
        dto.challengeName = sub.challenge.displayName;
        dto.username = sub.submittedBy.username;
        dto.displayName = sub.submittedBy.username;
        dto.noteText = sub.noteText;
        dto.submittedAt = sub.submittedTs.toString();
        return dto;
    }

    @GET
    public List<SubmissionDto> list(@QueryParam("status") String statusParam) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        List<ChallengeSubmission> submissions = (statusParam != null && !statusParam.isBlank())
                ? submissionRepository.findByStatusAndCentreId(SubmissionStatus.valueOf(statusParam), admin.centre.id)
                : submissionRepository.list("submittedBy.centre.id", admin.centre.id);
        return submissions.stream().map(this::toDto).toList();
    }

    @GET
    @Path("/{id}")
    public SubmissionDto get(@PathParam("id") UUID id) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        ChallengeSubmission submission = submissionRepository.findById(id);
        if (submission == null || !submission.submittedBy.centre.id.equals(admin.centre.id)) {
            throw new WebApplicationException("Submission not found", 404);
        }
        return toDto(submission);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public SubmissionDto update(@PathParam("id") UUID id, ReviewRequest request) {
        AppUser admin = authenticatedUserProvider.getCurrentUser();
        ChallengeSubmission submission = submissionRepository.findById(id);
        if (submission == null || !submission.submittedBy.centre.id.equals(admin.centre.id)) {
            throw new WebApplicationException("Submission not found", 404);
        }
        if (request != null && request.reviewerComment != null) {
            submission.reviewerComment = request.reviewerComment;
        }
        submissionRepository.persist(submission);
        return toDto(submission);
    }
}
