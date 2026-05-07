package org.scottishtecharmy.wishaw.quarkus.model;

import java.time.Instant;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "challenge_submission")
public class ChallengeSubmission extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    public Challenge challenge;

    @Column(name = "note_text", columnDefinition = "clob")
    public String noteText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "submitted_ts", nullable = false)
    public Instant submittedTs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    public AppUser submittedBy;

    @Column(name = "reviewed_ts")
    public Instant reviewedTs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    public AppUser reviewedBy;

    @Column(name = "reviewer_comment", columnDefinition = "clob")
    public String reviewerComment;
}
