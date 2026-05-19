import { login } from "./auth-service";
import type { AccessTokenResponse, LoginRequest } from "./auth-service";
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
