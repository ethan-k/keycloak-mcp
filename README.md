# Keycloak MCP Server (Spring AI)

A Model Context Protocol (MCP) server built with **Spring AI** that provides comprehensive integration with the Keycloak REST API. This server enables AI assistants and other MCP clients to manage Keycloak resources including realms, users, groups, clients, and roles.

## Features

This MCP server provides tools for managing:

- **Realms**: Create, read, update, and delete realms
- **Users**: Full user lifecycle management including password resets and group membership
- **Groups**: Group management and member administration
- **Clients**: OAuth/OIDC client configuration and secret management
- **Roles**: Both realm-level and client-level role management
- **Role Mappings**: Assign roles to users

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.1**
- **Spring AI 1.0.0-M4** (with MCP support)
- **Spring WebFlux** (for reactive HTTP client)
- **Maven** (build tool)

## Installation

### Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- A running Keycloak instance
- Admin credentials for Keycloak

### Setup

1. Clone this repository:
```bash
git clone <repository-url>
cd keycloak-mcp
```

2. Build the project:
```bash
mvn clean package
```

3. The built JAR will be available at `target/keycloak-mcp-server-1.0.0.jar`

## Configuration

The server is configured using environment variables or application properties:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `KEYCLOAK_BASE_URL` | Base URL of your Keycloak instance | `http://localhost:8080` | No |
| `KEYCLOAK_REALM` | Realm to authenticate against | `master` | No |
| `KEYCLOAK_CLIENT_ID` | Client ID for authentication | `admin-cli` | No |
| `KEYCLOAK_CLIENT_SECRET` | Client secret (for client credentials flow) | - | Yes* |
| `KEYCLOAK_USERNAME` | Admin username (for password flow) | - | Yes* |
| `KEYCLOAK_PASSWORD` | Admin password (for password flow) | - | Yes* |

*Either `KEYCLOAK_CLIENT_SECRET` or both `KEYCLOAK_USERNAME` and `KEYCLOAK_PASSWORD` must be provided.

### Example Configuration

Create a `.env` file in the project root or set environment variables:

```bash
KEYCLOAK_BASE_URL=http://localhost:8080
KEYCLOAK_REALM=master
KEYCLOAK_CLIENT_ID=admin-cli
KEYCLOAK_USERNAME=admin
KEYCLOAK_PASSWORD=admin
```

Or for client credentials flow:

```bash
KEYCLOAK_BASE_URL=http://localhost:8080
KEYCLOAK_REALM=master
KEYCLOAK_CLIENT_ID=my-service-account
KEYCLOAK_CLIENT_SECRET=your-client-secret
```

## Running the Server

### Standalone Mode

```bash
java -jar target/keycloak-mcp-server-1.0.0.jar
```

Or with environment variables:

```bash
KEYCLOAK_BASE_URL=http://localhost:8080 \
KEYCLOAK_USERNAME=admin \
KEYCLOAK_PASSWORD=admin \
java -jar target/keycloak-mcp-server-1.0.0.jar
```

### Development Mode

```bash
mvn spring-boot:run
```

## Usage with Claude Desktop

Add this server to your Claude Desktop configuration:

### macOS

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "keycloak": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/keycloak-mcp/target/keycloak-mcp-server-1.0.0.jar"
      ],
      "env": {
        "KEYCLOAK_BASE_URL": "http://localhost:8080",
        "KEYCLOAK_REALM": "master",
        "KEYCLOAK_CLIENT_ID": "admin-cli",
        "KEYCLOAK_USERNAME": "admin",
        "KEYCLOAK_PASSWORD": "admin"
      }
    }
  }
}
```

### Windows

Edit `%APPDATA%\Claude\claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "keycloak": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\path\\to\\keycloak-mcp\\target\\keycloak-mcp-server-1.0.0.jar"
      ],
      "env": {
        "KEYCLOAK_BASE_URL": "http://localhost:8080",
        "KEYCLOAK_REALM": "master",
        "KEYCLOAK_CLIENT_ID": "admin-cli",
        "KEYCLOAK_USERNAME": "admin",
        "KEYCLOAK_PASSWORD": "admin"
      }
    }
  }
}
```

## Available Tools

### Realm Management

- `keycloakListRealms` - List all realms
- `keycloakGetRealm` - Get details of a specific realm
- `keycloakCreateRealm` - Create a new realm
- `keycloakUpdateRealm` - Update an existing realm
- `keycloakDeleteRealm` - Delete a realm

### User Management

- `keycloakListUsers` - List users in a realm (with search and pagination)
- `keycloakGetUser` - Get details of a specific user
- `keycloakCreateUser` - Create a new user
- `keycloakUpdateUser` - Update an existing user
- `keycloakDeleteUser` - Delete a user
- `keycloakResetUserPassword` - Reset a user's password
- `keycloakGetUserGroups` - Get groups that a user belongs to
- `keycloakAddUserToGroup` - Add a user to a group
- `keycloakRemoveUserFromGroup` - Remove a user from a group

### Group Management

- `keycloakListGroups` - List groups in a realm
- `keycloakGetGroup` - Get details of a specific group
- `keycloakCreateGroup` - Create a new group
- `keycloakUpdateGroup` - Update an existing group
- `keycloakDeleteGroup` - Delete a group
- `keycloakGetGroupMembers` - Get members of a group

### Client Management

- `keycloakListClients` - List clients in a realm
- `keycloakGetClient` - Get details of a specific client
- `keycloakCreateClient` - Create a new client
- `keycloakUpdateClient` - Update an existing client
- `keycloakDeleteClient` - Delete a client
- `keycloakGetClientSecret` - Get the secret of a client
- `keycloakRegenerateClientSecret` - Regenerate the secret of a client

### Role Management

- `keycloakListRealmRoles` - List realm roles
- `keycloakGetRealmRole` - Get details of a specific realm role
- `keycloakCreateRealmRole` - Create a new realm role
- `keycloakUpdateRealmRole` - Update an existing realm role
- `keycloakDeleteRealmRole` - Delete a realm role
- `keycloakListClientRoles` - List client roles
- `keycloakGetClientRole` - Get details of a specific client role
- `keycloakCreateClientRole` - Create a new client role
- `keycloakUpdateClientRole` - Update an existing client role
- `keycloakDeleteClientRole` - Delete a client role
- `keycloakGetUserRealmRoles` - Get realm roles assigned to a user
- `keycloakAddRealmRolesToUser` - Add realm roles to a user
- `keycloakGetUserClientRoles` - Get client roles assigned to a user
- `keycloakAddClientRolesToUser` - Add client roles to a user

## Example Usage

Once configured in Claude Desktop, you can interact with Keycloak using natural language:

```
You: List all users in the master realm

Claude: [Uses keycloakListUsers tool to fetch users]

You: Create a new user named john.doe with email john.doe@example.com

Claude: [Uses keycloakCreateUser tool to create the user]

You: Reset the password for user ID abc-123 to "newpassword" and make it temporary

Claude: [Uses keycloakResetUserPassword tool]

You: Show me all clients in the production realm

Claude: [Uses keycloakListClients tool]
```

## Project Structure

```
keycloak-mcp/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/keycloak/mcp/
│       │       ├── KeycloakMcpServerApplication.java  # Main application
│       │       ├── client/
│       │       │   └── KeycloakClient.java            # Keycloak REST API client
│       │       ├── config/
│       │       │   └── KeycloakProperties.java        # Configuration properties
│       │       └── tools/
│       │           ├── RealmTools.java                # Realm management tools
│       │           ├── UserTools.java                 # User management tools
│       │           ├── GroupTools.java                # Group management tools
│       │           ├── ClientTools.java               # Client management tools
│       │           └── RoleTools.java                 # Role management tools
│       └── resources/
│           └── application.yml                         # Application configuration
├── pom.xml                                             # Maven configuration
├── .gitignore                                          # Git ignore rules
├── .env.example                                        # Example environment variables
└── README.md                                           # This file
```

## Development

### Building from Source

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Running in IDE

Import the project as a Maven project in your favorite IDE (IntelliJ IDEA, Eclipse, VS Code) and run the `KeycloakMcpServerApplication` class.

## Authentication

The server supports two authentication flows:

### 1. Password Grant (Default for admin-cli)

Uses username and password to authenticate. Best for development and testing.

```bash
KEYCLOAK_USERNAME=admin
KEYCLOAK_PASSWORD=admin
```

### 2. Client Credentials Grant

Uses client ID and secret. Best for production service accounts.

```bash
KEYCLOAK_CLIENT_ID=my-service-account
KEYCLOAK_CLIENT_SECRET=your-client-secret
```

The server automatically manages access tokens and refreshes them when they expire using Spring WebFlux reactive programming.

## Spring AI MCP Integration

This server leverages Spring AI's MCP support, which provides:

- **Automatic tool registration**: Java methods annotated with `@Bean` and `@Description` are automatically exposed as MCP tools
- **Type-safe request/response**: Uses Java records for request DTOs with schema annotations
- **Reactive programming**: Built on Spring WebFlux for non-blocking I/O
- **Configuration management**: Spring Boot's configuration properties for easy setup

## Security Considerations

1. **Credentials**: Never commit credentials to version control. Use environment variables or secure secret management.
2. **Access Control**: Ensure the service account or user has appropriate permissions in Keycloak.
3. **Network Security**: Use HTTPS in production environments.
4. **Token Management**: The server handles token refresh automatically using reactive streams.

## Troubleshooting

### Authentication Errors

If you see authentication errors:
1. Verify your credentials are correct
2. Ensure the user/client has admin permissions
3. Check that the realm name is correct
4. Verify the Keycloak base URL is accessible

### Connection Errors

If you can't connect to Keycloak:
1. Verify the `KEYCLOAK_BASE_URL` is correct
2. Ensure Keycloak is running and accessible
3. Check network/firewall settings

### Build Errors

If Maven build fails:
1. Ensure you have Java 21 installed: `java -version`
2. Ensure you have Maven 3.9+: `mvn -version`
3. Clean the project: `mvn clean`
4. Check that Spring AI repositories are accessible

## API Reference

For detailed information about the Keycloak REST API, see the [official documentation](https://www.keycloak.org/docs-api/latest/rest-api/index.html).

For more information about Spring AI and MCP, see:
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/)

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
