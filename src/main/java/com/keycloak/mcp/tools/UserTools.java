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
public class UserTools {

    private final KeycloakClient keycloakClient;

    @Bean
    @Description("List users in a realm")
    public Function<ListUsersRequest, List<Map<String, Object>>> keycloakListUsers() {
        return request -> {
            Map<String, String> params = new HashMap<>();
            if (request.search() != null) params.put("search", request.search());
            if (request.max() != null) params.put("max", request.max().toString());
            if (request.first() != null) params.put("first", request.first().toString());
            return keycloakClient.getUsers(request.realm(), params).block();
        };
    }

    @Bean
    @Description("Get details of a specific user")
    public Function<GetUserRequest, Map<String, Object>> keycloakGetUser() {
        return request -> keycloakClient.getUser(request.realm(), request.userId()).block();
    }

    @Bean
    @Description("Create a new user")
    public Function<CreateUserRequest, Map<String, Object>> keycloakCreateUser() {
        return request -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", request.username());
            userData.put("enabled", request.enabled() != null ? request.enabled() : true);
            if (request.email() != null) userData.put("email", request.email());
            if (request.firstName() != null) userData.put("firstName", request.firstName());
            if (request.lastName() != null) userData.put("lastName", request.lastName());
            if (request.config() != null) userData.putAll(request.config());

            keycloakClient.createUser(request.realm(), userData).block();
            return Map.of("success", true, "message", "User " + request.username() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing user")
    public Function<UpdateUserRequest, Map<String, Object>> keycloakUpdateUser() {
        return request -> {
            keycloakClient.updateUser(request.realm(), request.userId(), request.config()).block();
            return Map.of("success", true, "message", "User " + request.userId() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a user")
    public Function<DeleteUserRequest, Map<String, Object>> keycloakDeleteUser() {
        return request -> {
            keycloakClient.deleteUser(request.realm(), request.userId()).block();
            return Map.of("success", true, "message", "User " + request.userId() + " deleted successfully");
        };
    }

    @Bean
    @Description("Reset a user's password")
    public Function<ResetPasswordRequest, Map<String, Object>> keycloakResetUserPassword() {
        return request -> {
            boolean temporary = request.temporary() != null ? request.temporary() : true;
            keycloakClient.resetUserPassword(request.realm(), request.userId(), request.password(), temporary).block();
            return Map.of("success", true, "message", "Password reset for user " + request.userId());
        };
    }

    @Bean
    @Description("Get groups that a user belongs to")
    public Function<GetUserGroupsRequest, List<Map<String, Object>>> keycloakGetUserGroups() {
        return request -> keycloakClient.getUserGroups(request.realm(), request.userId()).block();
    }

    @Bean
    @Description("Add a user to a group")
    public Function<AddUserToGroupRequest, Map<String, Object>> keycloakAddUserToGroup() {
        return request -> {
            keycloakClient.addUserToGroup(request.realm(), request.userId(), request.groupId()).block();
            return Map.of("success", true, "message", "User " + request.userId() + " added to group " + request.groupId());
        };
    }

    @Bean
    @Description("Remove a user from a group")
    public Function<RemoveUserFromGroupRequest, Map<String, Object>> keycloakRemoveUserFromGroup() {
        return request -> {
            keycloakClient.removeUserFromGroup(request.realm(), request.userId(), request.groupId()).block();
            return Map.of("success", true, "message", "User " + request.userId() + " removed from group " + request.groupId());
        };
    }

    // Request DTOs
    public record ListUsersRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Search query to filter users") String search,
            @McpSchema(description = "Maximum number of users to return") Integer max,
            @McpSchema(description = "Offset for pagination") Integer first
    ) {}

    public record GetUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId
    ) {}

    public record CreateUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Username") String username,
            @McpSchema(description = "Email address") String email,
            @McpSchema(description = "First name") String firstName,
            @McpSchema(description = "Last name") String lastName,
            @McpSchema(description = "Whether the user is enabled") Boolean enabled,
            @McpSchema(description = "Additional user configuration") Map<String, Object> config
    ) {}

    public record UpdateUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "User configuration to update") Map<String, Object> config
    ) {}

    public record DeleteUserRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId
    ) {}

    public record ResetPasswordRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "New password") String password,
            @McpSchema(description = "Whether the password is temporary (user must change on next login)") Boolean temporary
    ) {}

    public record GetUserGroupsRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId
    ) {}

    public record AddUserToGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "ID of the group") String groupId
    ) {}

    public record RemoveUserFromGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the user") String userId,
            @McpSchema(description = "ID of the group") String groupId
    ) {}
}
