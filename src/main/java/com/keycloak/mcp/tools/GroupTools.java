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
public class GroupTools {

    private final KeycloakClient keycloakClient;

    @Bean
    @Description("List groups in a realm")
    public Function<ListGroupsRequest, List<Map<String, Object>>> keycloakListGroups() {
        return request -> {
            Map<String, String> params = new HashMap<>();
            if (request.search() != null) params.put("search", request.search());
            if (request.max() != null) params.put("max", request.max().toString());
            if (request.first() != null) params.put("first", request.first().toString());
            return keycloakClient.getGroups(request.realm(), params).block();
        };
    }

    @Bean
    @Description("Get details of a specific group")
    public Function<GetGroupRequest, Map<String, Object>> keycloakGetGroup() {
        return request -> keycloakClient.getGroup(request.realm(), request.groupId()).block();
    }

    @Bean
    @Description("Create a new group")
    public Function<CreateGroupRequest, Map<String, Object>> keycloakCreateGroup() {
        return request -> {
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("name", request.name());
            if (request.config() != null) {
                groupData.putAll(request.config());
            }
            keycloakClient.createGroup(request.realm(), groupData).block();
            return Map.of("success", true, "message", "Group " + request.name() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing group")
    public Function<UpdateGroupRequest, Map<String, Object>> keycloakUpdateGroup() {
        return request -> {
            keycloakClient.updateGroup(request.realm(), request.groupId(), request.config()).block();
            return Map.of("success", true, "message", "Group " + request.groupId() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a group")
    public Function<DeleteGroupRequest, Map<String, Object>> keycloakDeleteGroup() {
        return request -> {
            keycloakClient.deleteGroup(request.realm(), request.groupId()).block();
            return Map.of("success", true, "message", "Group " + request.groupId() + " deleted successfully");
        };
    }

    @Bean
    @Description("Get members of a group")
    public Function<GetGroupMembersRequest, List<Map<String, Object>>> keycloakGetGroupMembers() {
        return request -> keycloakClient.getGroupMembers(request.realm(), request.groupId()).block();
    }

    // Request DTOs
    public record ListGroupsRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Search query to filter groups") String search,
            @McpSchema(description = "Maximum number of groups to return") Integer max,
            @McpSchema(description = "Offset for pagination") Integer first
    ) {}

    public record GetGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the group") String groupId
    ) {}

    public record CreateGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Name of the group") String name,
            @McpSchema(description = "Additional group configuration") Map<String, Object> config
    ) {}

    public record UpdateGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the group") String groupId,
            @McpSchema(description = "Group configuration to update") Map<String, Object> config
    ) {}

    public record DeleteGroupRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the group") String groupId
    ) {}

    public record GetGroupMembersRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "ID of the group") String groupId
    ) {}
}
