package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class CreateChallengeRequest {
    public UUID moduleId;
    public UUID metadataId;
    public String displayName;
    public String description;
    public UUID badgeCategoryId;
    public int xpValue;
}

