package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class ChallengeDto {
    public UUID challengeId;
    public UUID moduleId;
    public UUID badgeCategoryId;
    public String displayName;
    public String description;
    public int xpValue;
}

