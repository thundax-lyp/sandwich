import { login } from "../api/auth-api";
import type { AccessTokenResponse, LoginRequest } from "../api/auth-api";
import { listCurrentUserPerms } from "../service/current-user-service";
import { clearPermissions, replacePermissions } from "./permission-storage";
import { clearAccessToken, saveTokenSession } from "./token-storage";

export const loginWithPermissions = async (request: LoginRequest): Promise<AccessTokenResponse> => {
    clearPermissions();
    const response = await login(request);
    saveTokenSession(response);

    try {
        const currentUserPerms = await listCurrentUserPerms();
        replacePermissions(currentUserPerms.perms || []);
    } catch (error) {
        clearAccessToken();
        throw error;
    }

    return response;
};
