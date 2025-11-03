import axios, { AxiosInstance } from 'axios';

export interface KeycloakConfig {
  baseUrl: string;
  realm: string;
  clientId: string;
  clientSecret?: string;
  username?: string;
  password?: string;
}

export class KeycloakClient {
  private axiosInstance: AxiosInstance;
  private config: KeycloakConfig;
  private accessToken: string | null = null;
  private tokenExpiry: number = 0;

  constructor(config: KeycloakConfig) {
    this.config = config;
    this.axiosInstance = axios.create({
      baseURL: config.baseUrl,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add request interceptor to inject access token
    this.axiosInstance.interceptors.request.use(async (config) => {
      await this.ensureValidToken();
      if (this.accessToken) {
        config.headers.Authorization = `Bearer ${this.accessToken}`;
      }
      return config;
    });
  }

  /**
   * Ensure we have a valid access token
   */
  private async ensureValidToken(): Promise<void> {
    const now = Date.now();

    // Refresh token if it's expired or about to expire (within 30 seconds)
    if (!this.accessToken || now >= this.tokenExpiry - 30000) {
      await this.authenticate();
    }
  }

  /**
   * Authenticate with Keycloak to get an access token
   */
  private async authenticate(): Promise<void> {
    const tokenUrl = `${this.config.baseUrl}/realms/${this.config.realm}/protocol/openid-connect/token`;

    const params = new URLSearchParams();

    if (this.config.username && this.config.password) {
      // Password grant
      params.append('grant_type', 'password');
      params.append('client_id', this.config.clientId);
      if (this.config.clientSecret) {
        params.append('client_secret', this.config.clientSecret);
      }
      params.append('username', this.config.username);
      params.append('password', this.config.password);
    } else if (this.config.clientSecret) {
      // Client credentials grant
      params.append('grant_type', 'client_credentials');
      params.append('client_id', this.config.clientId);
      params.append('client_secret', this.config.clientSecret);
    } else {
      throw new Error('Either username/password or client_secret must be provided');
    }

    try {
      const response = await axios.post(tokenUrl, params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });

      this.accessToken = response.data.access_token;
      // Set expiry time (current time + expires_in seconds)
      this.tokenExpiry = Date.now() + (response.data.expires_in * 1000);
    } catch (error) {
      throw new Error(`Failed to authenticate with Keycloak: ${error}`);
    }
  }

  /**
   * Make a GET request to the Keycloak API
   */
  async get<T>(path: string, params?: Record<string, any>): Promise<T> {
    const response = await this.axiosInstance.get<T>(path, { params });
    return response.data;
  }

  /**
   * Make a POST request to the Keycloak API
   */
  async post<T>(path: string, data?: any): Promise<T> {
    const response = await this.axiosInstance.post<T>(path, data);
    return response.data;
  }

  /**
   * Make a PUT request to the Keycloak API
   */
  async put<T>(path: string, data?: any): Promise<T> {
    const response = await this.axiosInstance.put<T>(path, data);
    return response.data;
  }

  /**
   * Make a DELETE request to the Keycloak API
   */
  async delete(path: string): Promise<void> {
    await this.axiosInstance.delete(path);
  }

  // ===== Realm Management =====

  async getRealms() {
    return this.get<any[]>('/admin/realms');
  }

  async getRealm(realmName: string) {
    return this.get<any>(`/admin/realms/${realmName}`);
  }

  async createRealm(realm: any) {
    return this.post('/admin/realms', realm);
  }

  async updateRealm(realmName: string, realm: any) {
    return this.put(`/admin/realms/${realmName}`, realm);
  }

  async deleteRealm(realmName: string) {
    return this.delete(`/admin/realms/${realmName}`);
  }

  // ===== User Management =====

  async getUsers(realmName: string, params?: { search?: string; max?: number; first?: number }) {
    return this.get<any[]>(`/admin/realms/${realmName}/users`, params);
  }

  async getUser(realmName: string, userId: string) {
    return this.get<any>(`/admin/realms/${realmName}/users/${userId}`);
  }

  async createUser(realmName: string, user: any) {
    return this.post(`/admin/realms/${realmName}/users`, user);
  }

  async updateUser(realmName: string, userId: string, user: any) {
    return this.put(`/admin/realms/${realmName}/users/${userId}`, user);
  }

  async deleteUser(realmName: string, userId: string) {
    return this.delete(`/admin/realms/${realmName}/users/${userId}`);
  }

  async resetUserPassword(realmName: string, userId: string, password: string, temporary: boolean = true) {
    return this.put(`/admin/realms/${realmName}/users/${userId}/reset-password`, {
      type: 'password',
      value: password,
      temporary,
    });
  }

  async getUserGroups(realmName: string, userId: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/users/${userId}/groups`);
  }

  async addUserToGroup(realmName: string, userId: string, groupId: string) {
    return this.put(`/admin/realms/${realmName}/users/${userId}/groups/${groupId}`, {});
  }

  async removeUserFromGroup(realmName: string, userId: string, groupId: string) {
    return this.delete(`/admin/realms/${realmName}/users/${userId}/groups/${groupId}`);
  }

  // ===== Group Management =====

  async getGroups(realmName: string, params?: { search?: string; max?: number; first?: number }) {
    return this.get<any[]>(`/admin/realms/${realmName}/groups`, params);
  }

  async getGroup(realmName: string, groupId: string) {
    return this.get<any>(`/admin/realms/${realmName}/groups/${groupId}`);
  }

  async createGroup(realmName: string, group: any) {
    return this.post(`/admin/realms/${realmName}/groups`, group);
  }

  async updateGroup(realmName: string, groupId: string, group: any) {
    return this.put(`/admin/realms/${realmName}/groups/${groupId}`, group);
  }

  async deleteGroup(realmName: string, groupId: string) {
    return this.delete(`/admin/realms/${realmName}/groups/${groupId}`);
  }

  async getGroupMembers(realmName: string, groupId: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/groups/${groupId}/members`);
  }

  // ===== Client Management =====

  async getClients(realmName: string, params?: { clientId?: string }) {
    return this.get<any[]>(`/admin/realms/${realmName}/clients`, params);
  }

  async getClient(realmName: string, clientUuid: string) {
    return this.get<any>(`/admin/realms/${realmName}/clients/${clientUuid}`);
  }

  async createClient(realmName: string, client: any) {
    return this.post(`/admin/realms/${realmName}/clients`, client);
  }

  async updateClient(realmName: string, clientUuid: string, client: any) {
    return this.put(`/admin/realms/${realmName}/clients/${clientUuid}`, client);
  }

  async deleteClient(realmName: string, clientUuid: string) {
    return this.delete(`/admin/realms/${realmName}/clients/${clientUuid}`);
  }

  async getClientSecret(realmName: string, clientUuid: string) {
    return this.get<any>(`/admin/realms/${realmName}/clients/${clientUuid}/client-secret`);
  }

  async regenerateClientSecret(realmName: string, clientUuid: string) {
    return this.post<any>(`/admin/realms/${realmName}/clients/${clientUuid}/client-secret`, {});
  }

  // ===== Role Management =====

  async getRealmRoles(realmName: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/roles`);
  }

  async getRealmRole(realmName: string, roleName: string) {
    return this.get<any>(`/admin/realms/${realmName}/roles/${roleName}`);
  }

  async createRealmRole(realmName: string, role: any) {
    return this.post(`/admin/realms/${realmName}/roles`, role);
  }

  async updateRealmRole(realmName: string, roleName: string, role: any) {
    return this.put(`/admin/realms/${realmName}/roles/${roleName}`, role);
  }

  async deleteRealmRole(realmName: string, roleName: string) {
    return this.delete(`/admin/realms/${realmName}/roles/${roleName}`);
  }

  async getClientRoles(realmName: string, clientUuid: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/clients/${clientUuid}/roles`);
  }

  async getClientRole(realmName: string, clientUuid: string, roleName: string) {
    return this.get<any>(`/admin/realms/${realmName}/clients/${clientUuid}/roles/${roleName}`);
  }

  async createClientRole(realmName: string, clientUuid: string, role: any) {
    return this.post(`/admin/realms/${realmName}/clients/${clientUuid}/roles`, role);
  }

  async updateClientRole(realmName: string, clientUuid: string, roleName: string, role: any) {
    return this.put(`/admin/realms/${realmName}/clients/${clientUuid}/roles/${roleName}`, role);
  }

  async deleteClientRole(realmName: string, clientUuid: string, roleName: string) {
    return this.delete(`/admin/realms/${realmName}/clients/${clientUuid}/roles/${roleName}`);
  }

  async getUserRealmRoles(realmName: string, userId: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/users/${userId}/role-mappings/realm`);
  }

  async addRealmRolesToUser(realmName: string, userId: string, roles: any[]) {
    return this.post(`/admin/realms/${realmName}/users/${userId}/role-mappings/realm`, roles);
  }

  async removeRealmRolesFromUser(realmName: string, userId: string, roles: any[]) {
    return this.delete(`/admin/realms/${realmName}/users/${userId}/role-mappings/realm`);
  }

  async getUserClientRoles(realmName: string, userId: string, clientUuid: string) {
    return this.get<any[]>(`/admin/realms/${realmName}/users/${userId}/role-mappings/clients/${clientUuid}`);
  }

  async addClientRolesToUser(realmName: string, userId: string, clientUuid: string, roles: any[]) {
    return this.post(`/admin/realms/${realmName}/users/${userId}/role-mappings/clients/${clientUuid}`, roles);
  }
}
