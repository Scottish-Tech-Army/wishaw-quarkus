package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class UpdateUserRequest {
    public UUID metadataId;
    public UUID parentId;
    public String role;
    public Boolean active;
    public String password;
}

