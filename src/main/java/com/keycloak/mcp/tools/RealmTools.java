package com.keycloak.mcp.tools;

import com.keycloak.mcp.client.KeycloakClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class RealmTools {

    private final KeycloakClient keycloakClient;

    @Bean
    @Description("List all realms in Keycloak")
    public Function<Map<String, Object>, List<Map<String, Object>>> keycloakListRealms() {
        return input -> keycloakClient.getRealms().block();
    }

    @Bean
    @Description("Get details of a specific realm")
    public Function<GetRealmRequest, Map<String, Object>> keycloakGetRealm() {
        return request -> keycloakClient.getRealm(request.realm()).block();
    }

    @Bean
    @Description("Create a new realm")
    public Function<CreateRealmRequest, Map<String, Object>> keycloakCreateRealm() {
        return request -> {
            Map<String, Object> realmData = new HashMap<>();
            realmData.put("realm", request.realm());
            realmData.put("enabled", request.enabled() != null ? request.enabled() : true);
            if (request.displayName() != null) {
                realmData.put("displayName", request.displayName());
            }
            if (request.config() != null) {
                realmData.putAll(request.config());
            }
            keycloakClient.createRealm(realmData).block();
            return Map.of("success", true, "message", "Realm " + request.realm() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing realm")
    public Function<UpdateRealmRequest, Map<String, Object>> keycloakUpdateRealm() {
        return request -> {
            keycloakClient.updateRealm(request.realm(), request.config()).block();
            return Map.of("success", true, "message", "Realm " + request.realm() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a realm")
    public Function<DeleteRealmRequest, Map<String, Object>> keycloakDeleteRealm() {
        return request -> {
            keycloakClient.deleteRealm(request.realm()).block();
            return Map.of("success", true, "message", "Realm " + request.realm() + " deleted successfully");
        };
    }

    // Request DTOs
    public record GetRealmRequest(
            @McpSchema(description = "Name of the realm") String realm
    ) {}

    public record CreateRealmRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Display name of the realm") String displayName,
            @McpSchema(description = "Whether the realm is enabled") Boolean enabled,
            @McpSchema(description = "Additional realm configuration") Map<String, Object> config
    ) {}

    public record UpdateRealmRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Realm configuration to update") Map<String, Object> config
    ) {}

    public record DeleteRealmRequest(
            @McpSchema(description = "Name of the realm") String realm
    ) {}
}
