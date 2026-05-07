package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class UserDto {
    public UUID userId;
    public UUID centreId;
    public String username;
    public String displayName;
    public String role;
    public boolean active;
}
