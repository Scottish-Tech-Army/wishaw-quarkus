package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class UpdateModuleRequest {
    public UUID metadataId;
    public String displayName;
    public String description;
    public UUID gameId;
    public Boolean active;
}

