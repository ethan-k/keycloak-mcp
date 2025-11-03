#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
  Tool,
} from '@modelcontextprotocol/sdk/types.js';
import { KeycloakClient, KeycloakConfig } from './keycloak-client.js';

// Read configuration from environment variables
const config: KeycloakConfig = {
  baseUrl: process.env.KEYCLOAK_BASE_URL || 'http://localhost:8080',
  realm: process.env.KEYCLOAK_REALM || 'master',
  clientId: process.env.KEYCLOAK_CLIENT_ID || 'admin-cli',
  clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
  username: process.env.KEYCLOAK_USERNAME,
  password: process.env.KEYCLOAK_PASSWORD,
};

// Initialize Keycloak client
const keycloakClient = new KeycloakClient(config);

// Define all available tools
const tools: Tool[] = [
  // ===== Realm Management =====
  {
    name: 'keycloak_list_realms',
    description: 'List all realms in Keycloak',
    inputSchema: {
      type: 'object',
      properties: {},
    },
  },
  {
    name: 'keycloak_get_realm',
    description: 'Get details of a specific realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_create_realm',
    description: 'Create a new realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        displayName: {
          type: 'string',
          description: 'Display name of the realm',
        },
        enabled: {
          type: 'boolean',
          description: 'Whether the realm is enabled',
        },
        config: {
          type: 'object',
          description: 'Additional realm configuration as JSON object',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_update_realm',
    description: 'Update an existing realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        config: {
          type: 'object',
          description: 'Realm configuration to update as JSON object',
        },
      },
      required: ['realm', 'config'],
    },
  },
  {
    name: 'keycloak_delete_realm',
    description: 'Delete a realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
      },
      required: ['realm'],
    },
  },

  // ===== User Management =====
  {
    name: 'keycloak_list_users',
    description: 'List users in a realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        search: {
          type: 'string',
          description: 'Search query to filter users',
        },
        max: {
          type: 'number',
          description: 'Maximum number of users to return',
        },
        first: {
          type: 'number',
          description: 'Offset for pagination',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_get_user',
    description: 'Get details of a specific user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
      },
      required: ['realm', 'userId'],
    },
  },
  {
    name: 'keycloak_create_user',
    description: 'Create a new user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        username: {
          type: 'string',
          description: 'Username',
        },
        email: {
          type: 'string',
          description: 'Email address',
        },
        firstName: {
          type: 'string',
          description: 'First name',
        },
        lastName: {
          type: 'string',
          description: 'Last name',
        },
        enabled: {
          type: 'boolean',
          description: 'Whether the user is enabled',
        },
        config: {
          type: 'object',
          description: 'Additional user configuration as JSON object',
        },
      },
      required: ['realm', 'username'],
    },
  },
  {
    name: 'keycloak_update_user',
    description: 'Update an existing user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        config: {
          type: 'object',
          description: 'User configuration to update as JSON object',
        },
      },
      required: ['realm', 'userId', 'config'],
    },
  },
  {
    name: 'keycloak_delete_user',
    description: 'Delete a user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
      },
      required: ['realm', 'userId'],
    },
  },
  {
    name: 'keycloak_reset_user_password',
    description: 'Reset a user\'s password',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        password: {
          type: 'string',
          description: 'New password',
        },
        temporary: {
          type: 'boolean',
          description: 'Whether the password is temporary (user must change on next login)',
        },
      },
      required: ['realm', 'userId', 'password'],
    },
  },
  {
    name: 'keycloak_get_user_groups',
    description: 'Get groups that a user belongs to',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
      },
      required: ['realm', 'userId'],
    },
  },
  {
    name: 'keycloak_add_user_to_group',
    description: 'Add a user to a group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
      },
      required: ['realm', 'userId', 'groupId'],
    },
  },
  {
    name: 'keycloak_remove_user_from_group',
    description: 'Remove a user from a group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
      },
      required: ['realm', 'userId', 'groupId'],
    },
  },

  // ===== Group Management =====
  {
    name: 'keycloak_list_groups',
    description: 'List groups in a realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        search: {
          type: 'string',
          description: 'Search query to filter groups',
        },
        max: {
          type: 'number',
          description: 'Maximum number of groups to return',
        },
        first: {
          type: 'number',
          description: 'Offset for pagination',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_get_group',
    description: 'Get details of a specific group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
      },
      required: ['realm', 'groupId'],
    },
  },
  {
    name: 'keycloak_create_group',
    description: 'Create a new group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        name: {
          type: 'string',
          description: 'Name of the group',
        },
        config: {
          type: 'object',
          description: 'Additional group configuration as JSON object',
        },
      },
      required: ['realm', 'name'],
    },
  },
  {
    name: 'keycloak_update_group',
    description: 'Update an existing group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
        config: {
          type: 'object',
          description: 'Group configuration to update as JSON object',
        },
      },
      required: ['realm', 'groupId', 'config'],
    },
  },
  {
    name: 'keycloak_delete_group',
    description: 'Delete a group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
      },
      required: ['realm', 'groupId'],
    },
  },
  {
    name: 'keycloak_get_group_members',
    description: 'Get members of a group',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        groupId: {
          type: 'string',
          description: 'ID of the group',
        },
      },
      required: ['realm', 'groupId'],
    },
  },

  // ===== Client Management =====
  {
    name: 'keycloak_list_clients',
    description: 'List clients in a realm',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientId: {
          type: 'string',
          description: 'Filter by client ID',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_get_client',
    description: 'Get details of a specific client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'clientUuid'],
    },
  },
  {
    name: 'keycloak_create_client',
    description: 'Create a new client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientId: {
          type: 'string',
          description: 'Client ID',
        },
        config: {
          type: 'object',
          description: 'Additional client configuration as JSON object',
        },
      },
      required: ['realm', 'clientId'],
    },
  },
  {
    name: 'keycloak_update_client',
    description: 'Update an existing client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        config: {
          type: 'object',
          description: 'Client configuration to update as JSON object',
        },
      },
      required: ['realm', 'clientUuid', 'config'],
    },
  },
  {
    name: 'keycloak_delete_client',
    description: 'Delete a client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'clientUuid'],
    },
  },
  {
    name: 'keycloak_get_client_secret',
    description: 'Get the secret of a client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'clientUuid'],
    },
  },
  {
    name: 'keycloak_regenerate_client_secret',
    description: 'Regenerate the secret of a client',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'clientUuid'],
    },
  },

  // ===== Role Management =====
  {
    name: 'keycloak_list_realm_roles',
    description: 'List realm roles',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
      },
      required: ['realm'],
    },
  },
  {
    name: 'keycloak_get_realm_role',
    description: 'Get details of a specific realm role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
      },
      required: ['realm', 'roleName'],
    },
  },
  {
    name: 'keycloak_create_realm_role',
    description: 'Create a new realm role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        name: {
          type: 'string',
          description: 'Name of the role',
        },
        description: {
          type: 'string',
          description: 'Description of the role',
        },
        config: {
          type: 'object',
          description: 'Additional role configuration as JSON object',
        },
      },
      required: ['realm', 'name'],
    },
  },
  {
    name: 'keycloak_update_realm_role',
    description: 'Update an existing realm role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
        config: {
          type: 'object',
          description: 'Role configuration to update as JSON object',
        },
      },
      required: ['realm', 'roleName', 'config'],
    },
  },
  {
    name: 'keycloak_delete_realm_role',
    description: 'Delete a realm role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
      },
      required: ['realm', 'roleName'],
    },
  },
  {
    name: 'keycloak_list_client_roles',
    description: 'List client roles',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'clientUuid'],
    },
  },
  {
    name: 'keycloak_get_client_role',
    description: 'Get details of a specific client role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
      },
      required: ['realm', 'clientUuid', 'roleName'],
    },
  },
  {
    name: 'keycloak_create_client_role',
    description: 'Create a new client role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        name: {
          type: 'string',
          description: 'Name of the role',
        },
        description: {
          type: 'string',
          description: 'Description of the role',
        },
        config: {
          type: 'object',
          description: 'Additional role configuration as JSON object',
        },
      },
      required: ['realm', 'clientUuid', 'name'],
    },
  },
  {
    name: 'keycloak_update_client_role',
    description: 'Update an existing client role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
        config: {
          type: 'object',
          description: 'Role configuration to update as JSON object',
        },
      },
      required: ['realm', 'clientUuid', 'roleName', 'config'],
    },
  },
  {
    name: 'keycloak_delete_client_role',
    description: 'Delete a client role',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        roleName: {
          type: 'string',
          description: 'Name of the role',
        },
      },
      required: ['realm', 'clientUuid', 'roleName'],
    },
  },
  {
    name: 'keycloak_get_user_realm_roles',
    description: 'Get realm roles assigned to a user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
      },
      required: ['realm', 'userId'],
    },
  },
  {
    name: 'keycloak_add_realm_roles_to_user',
    description: 'Add realm roles to a user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        roles: {
          type: 'array',
          description: 'Array of role objects to add (each with id and name)',
          items: {
            type: 'object',
          },
        },
      },
      required: ['realm', 'userId', 'roles'],
    },
  },
  {
    name: 'keycloak_get_user_client_roles',
    description: 'Get client roles assigned to a user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
      },
      required: ['realm', 'userId', 'clientUuid'],
    },
  },
  {
    name: 'keycloak_add_client_roles_to_user',
    description: 'Add client roles to a user',
    inputSchema: {
      type: 'object',
      properties: {
        realm: {
          type: 'string',
          description: 'Name of the realm',
        },
        userId: {
          type: 'string',
          description: 'ID of the user',
        },
        clientUuid: {
          type: 'string',
          description: 'UUID of the client',
        },
        roles: {
          type: 'array',
          description: 'Array of role objects to add (each with id and name)',
          items: {
            type: 'object',
          },
        },
      },
      required: ['realm', 'userId', 'clientUuid', 'roles'],
    },
  },
];

// Create server instance
const server = new Server(
  {
    name: 'keycloak-mcp-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// Handle tool listing
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return { tools };
});

// Handle tool execution
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    let result: any;

    switch (name) {
      // ===== Realm Management =====
      case 'keycloak_list_realms':
        result = await keycloakClient.getRealms();
        break;

      case 'keycloak_get_realm':
        result = await keycloakClient.getRealm(args.realm as string);
        break;

      case 'keycloak_create_realm': {
        const realmData: any = {
          realm: args.realm,
          enabled: args.enabled ?? true,
        };
        if (args.displayName) realmData.displayName = args.displayName;
        if (args.config) Object.assign(realmData, args.config);
        result = await keycloakClient.createRealm(realmData);
        result = { success: true, message: `Realm ${args.realm} created successfully` };
        break;
      }

      case 'keycloak_update_realm':
        result = await keycloakClient.updateRealm(args.realm as string, args.config);
        result = { success: true, message: `Realm ${args.realm} updated successfully` };
        break;

      case 'keycloak_delete_realm':
        result = await keycloakClient.deleteRealm(args.realm as string);
        result = { success: true, message: `Realm ${args.realm} deleted successfully` };
        break;

      // ===== User Management =====
      case 'keycloak_list_users': {
        const params: any = {};
        if (args.search) params.search = args.search;
        if (args.max) params.max = args.max;
        if (args.first) params.first = args.first;
        result = await keycloakClient.getUsers(args.realm as string, params);
        break;
      }

      case 'keycloak_get_user':
        result = await keycloakClient.getUser(args.realm as string, args.userId as string);
        break;

      case 'keycloak_create_user': {
        const userData: any = {
          username: args.username,
          enabled: args.enabled ?? true,
        };
        if (args.email) userData.email = args.email;
        if (args.firstName) userData.firstName = args.firstName;
        if (args.lastName) userData.lastName = args.lastName;
        if (args.config) Object.assign(userData, args.config);
        result = await keycloakClient.createUser(args.realm as string, userData);
        result = { success: true, message: `User ${args.username} created successfully` };
        break;
      }

      case 'keycloak_update_user':
        result = await keycloakClient.updateUser(args.realm as string, args.userId as string, args.config);
        result = { success: true, message: `User ${args.userId} updated successfully` };
        break;

      case 'keycloak_delete_user':
        result = await keycloakClient.deleteUser(args.realm as string, args.userId as string);
        result = { success: true, message: `User ${args.userId} deleted successfully` };
        break;

      case 'keycloak_reset_user_password':
        result = await keycloakClient.resetUserPassword(
          args.realm as string,
          args.userId as string,
          args.password as string,
          args.temporary ?? true
        );
        result = { success: true, message: `Password reset for user ${args.userId}` };
        break;

      case 'keycloak_get_user_groups':
        result = await keycloakClient.getUserGroups(args.realm as string, args.userId as string);
        break;

      case 'keycloak_add_user_to_group':
        result = await keycloakClient.addUserToGroup(
          args.realm as string,
          args.userId as string,
          args.groupId as string
        );
        result = { success: true, message: `User ${args.userId} added to group ${args.groupId}` };
        break;

      case 'keycloak_remove_user_from_group':
        result = await keycloakClient.removeUserFromGroup(
          args.realm as string,
          args.userId as string,
          args.groupId as string
        );
        result = { success: true, message: `User ${args.userId} removed from group ${args.groupId}` };
        break;

      // ===== Group Management =====
      case 'keycloak_list_groups': {
        const params: any = {};
        if (args.search) params.search = args.search;
        if (args.max) params.max = args.max;
        if (args.first) params.first = args.first;
        result = await keycloakClient.getGroups(args.realm as string, params);
        break;
      }

      case 'keycloak_get_group':
        result = await keycloakClient.getGroup(args.realm as string, args.groupId as string);
        break;

      case 'keycloak_create_group': {
        const groupData: any = {
          name: args.name,
        };
        if (args.config) Object.assign(groupData, args.config);
        result = await keycloakClient.createGroup(args.realm as string, groupData);
        result = { success: true, message: `Group ${args.name} created successfully` };
        break;
      }

      case 'keycloak_update_group':
        result = await keycloakClient.updateGroup(args.realm as string, args.groupId as string, args.config);
        result = { success: true, message: `Group ${args.groupId} updated successfully` };
        break;

      case 'keycloak_delete_group':
        result = await keycloakClient.deleteGroup(args.realm as string, args.groupId as string);
        result = { success: true, message: `Group ${args.groupId} deleted successfully` };
        break;

      case 'keycloak_get_group_members':
        result = await keycloakClient.getGroupMembers(args.realm as string, args.groupId as string);
        break;

      // ===== Client Management =====
      case 'keycloak_list_clients': {
        const params: any = {};
        if (args.clientId) params.clientId = args.clientId;
        result = await keycloakClient.getClients(args.realm as string, params);
        break;
      }

      case 'keycloak_get_client':
        result = await keycloakClient.getClient(args.realm as string, args.clientUuid as string);
        break;

      case 'keycloak_create_client': {
        const clientData: any = {
          clientId: args.clientId,
        };
        if (args.config) Object.assign(clientData, args.config);
        result = await keycloakClient.createClient(args.realm as string, clientData);
        result = { success: true, message: `Client ${args.clientId} created successfully` };
        break;
      }

      case 'keycloak_update_client':
        result = await keycloakClient.updateClient(args.realm as string, args.clientUuid as string, args.config);
        result = { success: true, message: `Client ${args.clientUuid} updated successfully` };
        break;

      case 'keycloak_delete_client':
        result = await keycloakClient.deleteClient(args.realm as string, args.clientUuid as string);
        result = { success: true, message: `Client ${args.clientUuid} deleted successfully` };
        break;

      case 'keycloak_get_client_secret':
        result = await keycloakClient.getClientSecret(args.realm as string, args.clientUuid as string);
        break;

      case 'keycloak_regenerate_client_secret':
        result = await keycloakClient.regenerateClientSecret(args.realm as string, args.clientUuid as string);
        break;

      // ===== Role Management =====
      case 'keycloak_list_realm_roles':
        result = await keycloakClient.getRealmRoles(args.realm as string);
        break;

      case 'keycloak_get_realm_role':
        result = await keycloakClient.getRealmRole(args.realm as string, args.roleName as string);
        break;

      case 'keycloak_create_realm_role': {
        const roleData: any = {
          name: args.name,
        };
        if (args.description) roleData.description = args.description;
        if (args.config) Object.assign(roleData, args.config);
        result = await keycloakClient.createRealmRole(args.realm as string, roleData);
        result = { success: true, message: `Realm role ${args.name} created successfully` };
        break;
      }

      case 'keycloak_update_realm_role':
        result = await keycloakClient.updateRealmRole(args.realm as string, args.roleName as string, args.config);
        result = { success: true, message: `Realm role ${args.roleName} updated successfully` };
        break;

      case 'keycloak_delete_realm_role':
        result = await keycloakClient.deleteRealmRole(args.realm as string, args.roleName as string);
        result = { success: true, message: `Realm role ${args.roleName} deleted successfully` };
        break;

      case 'keycloak_list_client_roles':
        result = await keycloakClient.getClientRoles(args.realm as string, args.clientUuid as string);
        break;

      case 'keycloak_get_client_role':
        result = await keycloakClient.getClientRole(
          args.realm as string,
          args.clientUuid as string,
          args.roleName as string
        );
        break;

      case 'keycloak_create_client_role': {
        const roleData: any = {
          name: args.name,
        };
        if (args.description) roleData.description = args.description;
        if (args.config) Object.assign(roleData, args.config);
        result = await keycloakClient.createClientRole(args.realm as string, args.clientUuid as string, roleData);
        result = { success: true, message: `Client role ${args.name} created successfully` };
        break;
      }

      case 'keycloak_update_client_role':
        result = await keycloakClient.updateClientRole(
          args.realm as string,
          args.clientUuid as string,
          args.roleName as string,
          args.config
        );
        result = { success: true, message: `Client role ${args.roleName} updated successfully` };
        break;

      case 'keycloak_delete_client_role':
        result = await keycloakClient.deleteClientRole(
          args.realm as string,
          args.clientUuid as string,
          args.roleName as string
        );
        result = { success: true, message: `Client role ${args.roleName} deleted successfully` };
        break;

      case 'keycloak_get_user_realm_roles':
        result = await keycloakClient.getUserRealmRoles(args.realm as string, args.userId as string);
        break;

      case 'keycloak_add_realm_roles_to_user':
        result = await keycloakClient.addRealmRolesToUser(
          args.realm as string,
          args.userId as string,
          args.roles as any[]
        );
        result = { success: true, message: `Realm roles added to user ${args.userId}` };
        break;

      case 'keycloak_get_user_client_roles':
        result = await keycloakClient.getUserClientRoles(
          args.realm as string,
          args.userId as string,
          args.clientUuid as string
        );
        break;

      case 'keycloak_add_client_roles_to_user':
        result = await keycloakClient.addClientRolesToUser(
          args.realm as string,
          args.userId as string,
          args.clientUuid as string,
          args.roles as any[]
        );
        result = { success: true, message: `Client roles added to user ${args.userId}` };
        break;

      default:
        return {
          content: [
            {
              type: 'text',
              text: `Unknown tool: ${name}`,
            },
          ],
          isError: true,
        };
    }

    return {
      content: [
        {
          type: 'text',
          text: JSON.stringify(result, null, 2),
        },
      ],
    };
  } catch (error: any) {
    return {
      content: [
        {
          type: 'text',
          text: `Error: ${error.message}\n${error.response?.data ? JSON.stringify(error.response.data, null, 2) : ''}`,
        },
      ],
      isError: true,
    };
  }
});

// Start server
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Keycloak MCP Server running on stdio');
}

main().catch((error) => {
  console.error('Fatal error in main():', error);
  process.exit(1);
});
