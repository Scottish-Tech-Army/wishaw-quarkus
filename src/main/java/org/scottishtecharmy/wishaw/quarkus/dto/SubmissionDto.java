package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class SubmissionDto {
    public UUID submissionId;
    public UUID challengeId;
    public String challengeName;
    public String username;
    public String displayName;
    public String noteText;
    public String submittedAt;
}
