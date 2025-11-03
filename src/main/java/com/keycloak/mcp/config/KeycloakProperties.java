package com.keycloak.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    @NotBlank(message = "Keycloak base URL is required")
    private String baseUrl = "http://localhost:8080";

    @NotBlank(message = "Keycloak realm is required")
    private String realm = "master";

    @NotBlank(message = "Keycloak client ID is required")
    private String clientId = "admin-cli";

    private String clientSecret;

    private String username;

    private String password;

    public boolean usePasswordGrant() {
        return username != null && password != null;
    }

    public boolean useClientCredentials() {
        return clientSecret != null;
    }
}
