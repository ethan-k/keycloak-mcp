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
public class RoleTools {

    private final KeycloakClient keycloakClient;

    // ===== Realm Roles =====

    @Bean
    @Description("List realm roles")
    public Function<ListRealmRolesRequest, List<Map<String, Object>>> keycloakListRealmRoles() {
        return request -> keycloakClient.getRealmRoles(request.realm()).block();
    }

    @Bean
    @Description("Get details of a specific realm role")
    public Function<GetRealmRoleRequest, Map<String, Object>> keycloakGetRealmRole() {
        return request -> keycloakClient.getRealmRole(request.realm(), request.roleName()).block();
    }

    @Bean
    @Description("Create a new realm role")
    public Function<CreateRealmRoleRequest, Map<String, Object>> keycloakCreateRealmRole() {
        return request -> {
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("name", request.name());
            if (request.description() != null) {
                roleData.put("description", request.description());
            }
            if (request.config() != null) {
                roleData.putAll(request.config());
            }
            keycloakClient.createRealmRole(request.realm(), roleData).block();
            return Map.of("success", true, "message", "Realm role " + request.name() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing realm role")
    public Function<UpdateRealmRoleRequest, Map<String, Object>> keycloakUpdateRealmRole() {
        return request -> {
            keycloakClient.updateRealmRole(request.realm(), request.roleName(), request.config()).block();
            return Map.of("success", true, "message", "Realm role " + request.roleName() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a realm role")
    public Function<DeleteRealmRoleRequest, Map<String, Object>> keycloakDeleteRealmRole() {
        return request -> {
            keycloakClient.deleteRealmRole(request.realm(), request.roleName()).block();
            return Map.of("success", true, "message", "Realm role " + request.roleName() + " deleted successfully");
        };
    }

    // ===== Client Roles =====

    @Bean
    @Description("List client roles")
    public Function<ListClientRolesRequest, List<Map<String, Object>>> keycloakListClientRoles() {
        return request -> keycloakClient.getClientRoles(request.realm(), request.clientUuid()).block();
    }

    @Bean
    @Description("Get details of a specific client role")
    public Function<GetClientRoleRequest, Map<String, Object>> keycloakGetClientRole() {
        return request -> keycloakClient.getClientRole(request.realm(), request.clientUuid(), request.roleName()).block();
    }

    @Bean
    @Description("Create a new client role")
    public Function<CreateClientRoleRequest, Map<String, Object>> keycloakCreateClientRole() {
        return request -> {
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("name", request.name());
            if (request.description() != null) {
                roleData.put("description", request.description());
            }
            if (request.config() != null) {
                roleData.putAll(request.config());
            }
            keycloakClient.createClientRole(request.realm(), request.clientUuid(), roleData).block();
            return Map.of("success", true, "message", "Client role " + request.name() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing client role")
    public Function<UpdateClientRoleRequest, Map<String, Object>> keycloakUpdateClientRole() {
        return request -> {
            keycloakClient.updateClientRole(request.realm(), request.clientUuid(), request.roleName(), request.config()).block();
            return Map.of("success", true, "message", "Client role " + request.roleName() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a client role")
    public Function<DeleteClientRoleRequest, Map<String, Object>> keycloakDeleteClientRole() {
        return request -> {
            keycloakClient.deleteClientRole(request.realm(), request.clientUuid(), request.roleName()).block();
            return Map.of("success", true, "message", "Client role " + request.roleName() + " deleted successfully");
        };
    }

    // ===== User Role Mappings =====

    @Bean
    @Description("Get realm roles assigned to a user")
    public Function<GetUserRealmRolesRequest, List<Map<String, Object>>> keycloakGetUserRealmRoles() {
        return request -> keycloakClient.getUserRealmRoles(request.realm(), request.userId()).block();
    }

    @Bean
    @Description("Add realm roles to a user")
    public Function<AddRealmRolesToUserRequest, Map<String, Object>> keycloakAddRealmRolesToUser() {
        return request -> {
            keycloakClient.addRealmRolesToUser(request.realm(), request.userId(), request.roles()).block();
            return Map.of("success", true, "message", "Realm roles added to user " + request.userId());
        };
    }

    @Bean
    @Description("Get client roles assigned to a user")
    public Function<GetUserClientRolesRequest, List<Map<String, Object>>> keycloakGetUserClientRoles() {
        return request -> keycloakClient.getUserClientRoles(request.realm(), request.userId(), request.clientUuid()).block();
    }

    @Bean
    @Description("Add client roles to a user")
    public Function<AddClientRolesToUserRequest, Map<String, Object>> keycloakAddClientRolesToUser() {
        return request -> {
            keycloakClient.addClientRolesToUser(request.realm(), request.userId(), request.clientUuid(), request.roles()).block();
            return Map.of("success", true, "message", "Client roles added to user " + request.userId());
        };
    }

    // Request DTOs - Realm Roles
    public record ListRealmRolesRequest(
            @McpSchema(description = "Name of the realm") String realm
    ) {}

    public record GetRealmRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Name of the role") String roleName
    ) {}

    public record CreateRealmRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Name of the role") String name,
            @McpSchema(description = "Description of the role") String description,
            @McpSchema(description = "Additional role configuration") Map<String, Object> config
    ) {}

    public record UpdateRealmRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Name of the role") String roleName,
            @McpSchema(description = "Role configuration to update") Map<String, Object> config
    ) {}

    public record DeleteRealmRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Name of the role") String roleName
    ) {}

    // Request DTOs - Client Roles
    public record ListClientRolesRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}

    public record GetClientRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Name of the role") String roleName
    ) {}

    public record CreateClientRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Name of the role") String name,
            @McpSchema(description = "Description of the role") String description,
            @McpSchema(description = "Additional role configuration") Map<String, Object> config
    ) {}

    public record UpdateClientRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Name of the role") String roleName,
            @McpSchema(description = "Role configuration to update") Map<String, Object> config
    ) {}

    public record DeleteClientRoleRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Name of the role") String roleName
    ) {}

    // Request DTOs - User Role Mappings
    public record GetUserRealmRolesRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId
    ) {}

    public record AddRealmRolesToUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "Array of role objects to add") List<Map<String, Object>> roles
    ) {}

    public record GetUserClientRolesRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}

    public record AddClientRolesToUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Array of role objects to add") List<Map<String, Object>> roles
    ) {}
}
