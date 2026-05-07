package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class CreateModuleRequest {
    public UUID metadataId;
    public String displayName;
    public String description;
    public UUID gameId;
}

