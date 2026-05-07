package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class ManageModuleDto {
    public UUID moduleId;
    public UUID gameId;
    public String displayName;
    public String description;
    public boolean active;
}

