# Keycloak MCP Server

A Model Context Protocol (MCP) server that provides comprehensive integration with the Keycloak REST API. This server enables AI assistants and other MCP clients to manage Keycloak resources including realms, users, groups, clients, and roles.

## Features

This MCP server provides tools for managing:

- **Realms**: Create, read, update, and delete realms
- **Users**: Full user lifecycle management including password resets and group membership
- **Groups**: Group management and member administration
- **Clients**: OAuth/OIDC client configuration and secret management
- **Roles**: Both realm-level and client-level role management
- **Role Mappings**: Assign roles to users

## Installation

### Prerequisites

- Node.js 18 or higher
- A running Keycloak instance
- Admin credentials for Keycloak

### Setup

1. Clone this repository:
```bash
git clone <repository-url>
cd keycloak-mcp
```

2. Install dependencies:
```bash
npm install
```

3. Build the project:
```bash
npm run build
```

## Configuration

The server is configured using environment variables:

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

Create a `.env` file in the project root:

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

## Usage with Claude Desktop

Add this server to your Claude Desktop configuration:

### macOS

Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "keycloak": {
      "command": "node",
      "args": ["/absolute/path/to/keycloak-mcp/dist/index.js"],
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
      "command": "node",
      "args": ["C:\\path\\to\\keycloak-mcp\\dist\\index.js"],
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

- `keycloak_list_realms` - List all realms
- `keycloak_get_realm` - Get details of a specific realm
- `keycloak_create_realm` - Create a new realm
- `keycloak_update_realm` - Update an existing realm
- `keycloak_delete_realm` - Delete a realm

### User Management

- `keycloak_list_users` - List users in a realm (with search and pagination)
- `keycloak_get_user` - Get details of a specific user
- `keycloak_create_user` - Create a new user
- `keycloak_update_user` - Update an existing user
- `keycloak_delete_user` - Delete a user
- `keycloak_reset_user_password` - Reset a user's password
- `keycloak_get_user_groups` - Get groups that a user belongs to
- `keycloak_add_user_to_group` - Add a user to a group
- `keycloak_remove_user_from_group` - Remove a user from a group

### Group Management

- `keycloak_list_groups` - List groups in a realm
- `keycloak_get_group` - Get details of a specific group
- `keycloak_create_group` - Create a new group
- `keycloak_update_group` - Update an existing group
- `keycloak_delete_group` - Delete a group
- `keycloak_get_group_members` - Get members of a group

### Client Management

- `keycloak_list_clients` - List clients in a realm
- `keycloak_get_client` - Get details of a specific client
- `keycloak_create_client` - Create a new client
- `keycloak_update_client` - Update an existing client
- `keycloak_delete_client` - Delete a client
- `keycloak_get_client_secret` - Get the secret of a client
- `keycloak_regenerate_client_secret` - Regenerate the secret of a client

### Role Management

- `keycloak_list_realm_roles` - List realm roles
- `keycloak_get_realm_role` - Get details of a specific realm role
- `keycloak_create_realm_role` - Create a new realm role
- `keycloak_update_realm_role` - Update an existing realm role
- `keycloak_delete_realm_role` - Delete a realm role
- `keycloak_list_client_roles` - List client roles
- `keycloak_get_client_role` - Get details of a specific client role
- `keycloak_create_client_role` - Create a new client role
- `keycloak_update_client_role` - Update an existing client role
- `keycloak_delete_client_role` - Delete a client role
- `keycloak_get_user_realm_roles` - Get realm roles assigned to a user
- `keycloak_add_realm_roles_to_user` - Add realm roles to a user
- `keycloak_get_user_client_roles` - Get client roles assigned to a user
- `keycloak_add_client_roles_to_user` - Add client roles to a user

## Example Usage

Once configured in Claude Desktop, you can interact with Keycloak using natural language:

```
You: List all users in the master realm

Claude: [Uses keycloak_list_users tool to fetch users]

You: Create a new user named john.doe with email john.doe@example.com

Claude: [Uses keycloak_create_user tool to create the user]

You: Reset the password for user ID abc-123 to "newpassword" and make it temporary

Claude: [Uses keycloak_reset_user_password tool]

You: Show me all clients in the production realm

Claude: [Uses keycloak_list_clients tool]
```

## Development

### Running in Development Mode

```bash
npm run dev
```

### Building

```bash
npm run build
```

### Watch Mode

```bash
npm run watch
```

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

The server automatically manages access tokens and refreshes them when they expire.

## Security Considerations

1. **Credentials**: Never commit credentials to version control. Use environment variables or secure secret management.
2. **Access Control**: Ensure the service account or user has appropriate permissions in Keycloak.
3. **Network Security**: Use HTTPS in production environments.
4. **Token Management**: The server handles token refresh automatically, but tokens are stored in memory.

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

## API Reference

For detailed information about the Keycloak REST API, see the [official documentation](https://www.keycloak.org/docs-api/latest/rest-api/index.html).

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
