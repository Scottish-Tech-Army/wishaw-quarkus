package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.ChallengeSubmission;
import org.scottishtecharmy.wishaw.quarkus.model.SubmissionStatus;

@ApplicationScoped
public class ChallengeSubmissionRepository implements PanacheRepositoryBase<ChallengeSubmission, UUID> {

    public List<ChallengeSubmission> findByUserId(UUID userId) {
        return list("submittedBy.id", userId);
    }

    public List<ChallengeSubmission> findByUserIdAndStatus(UUID userId, SubmissionStatus status) {
        return list("submittedBy.id = ?1 and status = ?2", userId, status);
    }

    public List<ChallengeSubmission> findByStatusAndCentreId(SubmissionStatus status, UUID centreId) {
        return list("SELECT s FROM ChallengeSubmission s JOIN FETCH s.challenge JOIN FETCH s.submittedBy sb JOIN FETCH sb.centre WHERE s.status = ?1 AND sb.centre.id = ?2 ORDER BY s.submittedTs DESC", status, centreId);
    }

    public List<ChallengeSubmission> findApprovedByUserId(UUID userId) {
        return list("SELECT s FROM ChallengeSubmission s JOIN FETCH s.challenge JOIN FETCH s.submittedBy WHERE s.submittedBy.id = ?1 AND s.status = ?2", userId, SubmissionStatus.APPROVED);
    }
}
