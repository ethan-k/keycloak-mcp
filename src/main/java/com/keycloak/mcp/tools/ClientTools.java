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
public class ClientTools {

    private final KeycloakClient keycloakClient;

    @Bean
    @Description("List clients in a realm")
    public Function<ListClientsRequest, List<Map<String, Object>>> keycloakListClients() {
        return request -> {
            Map<String, String> params = new HashMap<>();
            if (request.clientId() != null) {
                params.put("clientId", request.clientId());
            }
            return keycloakClient.getClients(request.realm(), params).block();
        };
    }

    @Bean
    @Description("Get details of a specific client")
    public Function<GetClientRequest, Map<String, Object>> keycloakGetClient() {
        return request -> keycloakClient.getClient(request.realm(), request.clientUuid()).block();
    }

    @Bean
    @Description("Create a new client")
    public Function<CreateClientRequest, Map<String, Object>> keycloakCreateClient() {
        return request -> {
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("clientId", request.clientId());
            if (request.config() != null) {
                clientData.putAll(request.config());
            }
            keycloakClient.createClient(request.realm(), clientData).block();
            return Map.of("success", true, "message", "Client " + request.clientId() + " created successfully");
        };
    }

    @Bean
    @Description("Update an existing client")
    public Function<UpdateClientRequest, Map<String, Object>> keycloakUpdateClient() {
        return request -> {
            keycloakClient.updateClient(request.realm(), request.clientUuid(), request.config()).block();
            return Map.of("success", true, "message", "Client " + request.clientUuid() + " updated successfully");
        };
    }

    @Bean
    @Description("Delete a client")
    public Function<DeleteClientRequest, Map<String, Object>> keycloakDeleteClient() {
        return request -> {
            keycloakClient.deleteClient(request.realm(), request.clientUuid()).block();
            return Map.of("success", true, "message", "Client " + request.clientUuid() + " deleted successfully");
        };
    }

    @Bean
    @Description("Get the secret of a client")
    public Function<GetClientSecretRequest, Map<String, Object>> keycloakGetClientSecret() {
        return request -> keycloakClient.getClientSecret(request.realm(), request.clientUuid()).block();
    }

    @Bean
    @Description("Regenerate the secret of a client")
    public Function<RegenerateClientSecretRequest, Map<String, Object>> keycloakRegenerateClientSecret() {
        return request -> keycloakClient.regenerateClientSecret(request.realm(), request.clientUuid()).block();
    }

    // Request DTOs
    public record ListClientsRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Filter by client ID") String clientId
    ) {}

    public record GetClientRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}

    public record CreateClientRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "Client ID") String clientId,
            @McpSchema(description = "Additional client configuration") Map<String, Object> config
    ) {}

    public record UpdateClientRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid,
            @McpSchema(description = "Client configuration to update") Map<String, Object> config
    ) {}

    public record DeleteClientRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}

    public record GetClientSecretRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}

    public record RegenerateClientSecretRequest(
            @McpSchema(description = "Name of the realm") String realm,
            @McpSchema(description = "UUID of the client") String clientUuid
    ) {}
}
