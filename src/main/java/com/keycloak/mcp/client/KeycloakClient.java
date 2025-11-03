package com.keycloak.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.mcp.config.KeycloakProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KeycloakClient {

    private final WebClient webClient;
    private final WebClient authClient;
    private final KeycloakProperties properties;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant tokenExpiry;

    public KeycloakClient(KeycloakProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter((request, next) -> {
                    return ensureValidToken()
                            .flatMap(token -> {
                                var modifiedRequest = org.springframework.web.reactive.function.client.ClientRequest
                                        .from(request)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .build();
                                return next.exchange(modifiedRequest);
                            });
                })
                .build();

        this.authClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    private Mono<String> ensureValidToken() {
        Instant now = Instant.now();

        // Check if token is valid (exists and not expired within 30 seconds)
        if (accessToken != null && tokenExpiry != null && now.plusSeconds(30).isBefore(tokenExpiry)) {
            return Mono.just(accessToken);
        }

        return authenticate();
    }

    private Mono<String> authenticate() {
        String tokenUrl = String.format("/realms/%s/protocol/openid-connect/token", properties.getRealm());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        if (properties.usePasswordGrant()) {
            formData.add("grant_type", "password");
            formData.add("client_id", properties.getClientId());
            if (properties.getClientSecret() != null) {
                formData.add("client_secret", properties.getClientSecret());
            }
            formData.add("username", properties.getUsername());
            formData.add("password", properties.getPassword());
        } else if (properties.useClientCredentials()) {
            formData.add("grant_type", "client_credentials");
            formData.add("client_id", properties.getClientId());
            formData.add("client_secret", properties.getClientSecret());
        } else {
            return Mono.error(new IllegalStateException("Either username/password or client_secret must be provided"));
        }

        return authClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    this.accessToken = response.get("access_token").asText();
                    int expiresIn = response.get("expires_in").asInt();
                    this.tokenExpiry = Instant.now().plusSeconds(expiresIn);
                    log.info("Successfully authenticated with Keycloak");
                    return this.accessToken;
                })
                .doOnError(error -> log.error("Failed to authenticate with Keycloak", error));
    }

    // ===== Generic HTTP Methods =====

    public <T> Mono<T> get(String path, Class<T> responseType) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(responseType);
    }

    public <T> Mono<List<T>> getList(String path, Class<T> elementType) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    public <T> Mono<T> post(String path, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    public Mono<Void> post(String path, Object body) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public <T> Mono<T> put(String path, Object body, Class<T> responseType) {
        return webClient.put()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    public Mono<Void> put(String path, Object body) {
        return webClient.put()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> delete(String path) {
        return webClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // ===== Realm Management =====

    public Mono<List<Map<String, Object>>> getRealms() {
        return getList("/admin/realms", Map.class);
    }

    public Mono<Map<String, Object>> getRealm(String realmName) {
        return get(String.format("/admin/realms/%s", realmName), Map.class);
    }

    public Mono<Void> createRealm(Map<String, Object> realm) {
        return post("/admin/realms", realm);
    }

    public Mono<Void> updateRealm(String realmName, Map<String, Object> realm) {
        return put(String.format("/admin/realms/%s", realmName), realm);
    }

    public Mono<Void> deleteRealm(String realmName) {
        return delete(String.format("/admin/realms/%s", realmName));
    }

    // ===== User Management =====

    public Mono<List<Map<String, Object>>> getUsers(String realmName, Map<String, String> params) {
        StringBuilder uriBuilder = new StringBuilder(String.format("/admin/realms/%s/users", realmName));
        if (params != null && !params.isEmpty()) {
            uriBuilder.append("?");
            params.forEach((key, value) -> uriBuilder.append(key).append("=").append(value).append("&"));
        }
        return getList(uriBuilder.toString(), Map.class);
    }

    public Mono<Map<String, Object>> getUser(String realmName, String userId) {
        return get(String.format("/admin/realms/%s/users/%s", realmName, userId), Map.class);
    }

    public Mono<Void> createUser(String realmName, Map<String, Object> user) {
        return post(String.format("/admin/realms/%s/users", realmName), user);
    }

    public Mono<Void> updateUser(String realmName, String userId, Map<String, Object> user) {
        return put(String.format("/admin/realms/%s/users/%s", realmName, userId), user);
    }

    public Mono<Void> deleteUser(String realmName, String userId) {
        return delete(String.format("/admin/realms/%s/users/%s", realmName, userId));
    }

    public Mono<Void> resetUserPassword(String realmName, String userId, String password, boolean temporary) {
        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", password,
                "temporary", temporary
        );
        return put(String.format("/admin/realms/%s/users/%s/reset-password", realmName, userId), credentials);
    }

    public Mono<List<Map<String, Object>>> getUserGroups(String realmName, String userId) {
        return getList(String.format("/admin/realms/%s/users/%s/groups", realmName, userId), Map.class);
    }

    public Mono<Void> addUserToGroup(String realmName, String userId, String groupId) {
        return put(String.format("/admin/realms/%s/users/%s/groups/%s", realmName, userId, groupId), Map.of());
    }

    public Mono<Void> removeUserFromGroup(String realmName, String userId, String groupId) {
        return delete(String.format("/admin/realms/%s/users/%s/groups/%s", realmName, userId, groupId));
    }

    // ===== Group Management =====

    public Mono<List<Map<String, Object>>> getGroups(String realmName, Map<String, String> params) {
        StringBuilder uriBuilder = new StringBuilder(String.format("/admin/realms/%s/groups", realmName));
        if (params != null && !params.isEmpty()) {
            uriBuilder.append("?");
            params.forEach((key, value) -> uriBuilder.append(key).append("=").append(value).append("&"));
        }
        return getList(uriBuilder.toString(), Map.class);
    }

    public Mono<Map<String, Object>> getGroup(String realmName, String groupId) {
        return get(String.format("/admin/realms/%s/groups/%s", realmName, groupId), Map.class);
    }

    public Mono<Void> createGroup(String realmName, Map<String, Object> group) {
        return post(String.format("/admin/realms/%s/groups", realmName), group);
    }

    public Mono<Void> updateGroup(String realmName, String groupId, Map<String, Object> group) {
        return put(String.format("/admin/realms/%s/groups/%s", realmName, groupId), group);
    }

    public Mono<Void> deleteGroup(String realmName, String groupId) {
        return delete(String.format("/admin/realms/%s/groups/%s", realmName, groupId));
    }

    public Mono<List<Map<String, Object>>> getGroupMembers(String realmName, String groupId) {
        return getList(String.format("/admin/realms/%s/groups/%s/members", realmName, groupId), Map.class);
    }

    // ===== Client Management =====

    public Mono<List<Map<String, Object>>> getClients(String realmName, Map<String, String> params) {
        StringBuilder uriBuilder = new StringBuilder(String.format("/admin/realms/%s/clients", realmName));
        if (params != null && !params.isEmpty()) {
            uriBuilder.append("?");
            params.forEach((key, value) -> uriBuilder.append(key).append("=").append(value).append("&"));
        }
        return getList(uriBuilder.toString(), Map.class);
    }

    public Mono<Map<String, Object>> getClient(String realmName, String clientUuid) {
        return get(String.format("/admin/realms/%s/clients/%s", realmName, clientUuid), Map.class);
    }

    public Mono<Void> createClient(String realmName, Map<String, Object> client) {
        return post(String.format("/admin/realms/%s/clients", realmName), client);
    }

    public Mono<Void> updateClient(String realmName, String clientUuid, Map<String, Object> client) {
        return put(String.format("/admin/realms/%s/clients/%s", realmName, clientUuid), client);
    }

    public Mono<Void> deleteClient(String realmName, String clientUuid) {
        return delete(String.format("/admin/realms/%s/clients/%s", realmName, clientUuid));
    }

    public Mono<Map<String, Object>> getClientSecret(String realmName, String clientUuid) {
        return get(String.format("/admin/realms/%s/clients/%s/client-secret", realmName, clientUuid), Map.class);
    }

    public Mono<Map<String, Object>> regenerateClientSecret(String realmName, String clientUuid) {
        return post(String.format("/admin/realms/%s/clients/%s/client-secret", realmName, clientUuid), Map.of(), Map.class);
    }

    // ===== Role Management =====

    public Mono<List<Map<String, Object>>> getRealmRoles(String realmName) {
        return getList(String.format("/admin/realms/%s/roles", realmName), Map.class);
    }

    public Mono<Map<String, Object>> getRealmRole(String realmName, String roleName) {
        return get(String.format("/admin/realms/%s/roles/%s", realmName, roleName), Map.class);
    }

    public Mono<Void> createRealmRole(String realmName, Map<String, Object> role) {
        return post(String.format("/admin/realms/%s/roles", realmName), role);
    }

    public Mono<Void> updateRealmRole(String realmName, String roleName, Map<String, Object> role) {
        return put(String.format("/admin/realms/%s/roles/%s", realmName, roleName), role);
    }

    public Mono<Void> deleteRealmRole(String realmName, String roleName) {
        return delete(String.format("/admin/realms/%s/roles/%s", realmName, roleName));
    }

    public Mono<List<Map<String, Object>>> getClientRoles(String realmName, String clientUuid) {
        return getList(String.format("/admin/realms/%s/clients/%s/roles", realmName, clientUuid), Map.class);
    }

    public Mono<Map<String, Object>> getClientRole(String realmName, String clientUuid, String roleName) {
        return get(String.format("/admin/realms/%s/clients/%s/roles/%s", realmName, clientUuid, roleName), Map.class);
    }

    public Mono<Void> createClientRole(String realmName, String clientUuid, Map<String, Object> role) {
        return post(String.format("/admin/realms/%s/clients/%s/roles", realmName, clientUuid), role);
    }

    public Mono<Void> updateClientRole(String realmName, String clientUuid, String roleName, Map<String, Object> role) {
        return put(String.format("/admin/realms/%s/clients/%s/roles/%s", realmName, clientUuid, roleName), role);
    }

    public Mono<Void> deleteClientRole(String realmName, String clientUuid, String roleName) {
        return delete(String.format("/admin/realms/%s/clients/%s/roles/%s", realmName, clientUuid, roleName));
    }

    public Mono<List<Map<String, Object>>> getUserRealmRoles(String realmName, String userId) {
        return getList(String.format("/admin/realms/%s/users/%s/role-mappings/realm", realmName, userId), Map.class);
    }

    public Mono<Void> addRealmRolesToUser(String realmName, String userId, List<Map<String, Object>> roles) {
        return post(String.format("/admin/realms/%s/users/%s/role-mappings/realm", realmName, userId), roles);
    }

    public Mono<List<Map<String, Object>>> getUserClientRoles(String realmName, String userId, String clientUuid) {
        return getList(String.format("/admin/realms/%s/users/%s/role-mappings/clients/%s", realmName, userId, clientUuid), Map.class);
    }

    public Mono<Void> addClientRolesToUser(String realmName, String userId, String clientUuid, List<Map<String, Object>> roles) {
        return post(String.format("/admin/realms/%s/users/%s/role-mappings/clients/%s", realmName, userId, clientUuid), roles);
    }
}
