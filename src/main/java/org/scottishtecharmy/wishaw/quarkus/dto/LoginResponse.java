package org.scottishtecharmy.wishaw.quarkus.dto;

import java.util.UUID;

public class LoginResponse {
    public UUID userId;
    public String username;
    public String role;
    public UUID centreId;
    public String displayName;
}
