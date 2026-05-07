package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class CreateUserRequest {
    public UUID centreId;
    public UUID metadataId;
    public UUID parentId;
    public String username;
    public String password;
    public String role;
}

