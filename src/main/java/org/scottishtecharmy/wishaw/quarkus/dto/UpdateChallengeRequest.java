package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class UpdateChallengeRequest {
    public UUID metadataId;
    public String displayName;
    public String description;
    public UUID badgeCategoryId;
    public Integer xpValue;
}

