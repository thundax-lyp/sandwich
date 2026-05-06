import { clearPermissions } from "./permission-storage";

const ACCESS_TOKEN_KEY = "sandwish.admin.accessToken";
const ACCESS_TOKEN_CHANGE_EVENT = "sandwish.admin.accessToken.change";

const notifyAccessTokenChange = () => {
    window.dispatchEvent(new Event(ACCESS_TOKEN_CHANGE_EVENT));
};

export const getAccessToken = () => {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
};

export const saveAccessToken = (token: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
    notifyAccessTokenChange();
};

export const clearAccessToken = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    clearPermissions();
    notifyAccessTokenChange();
};

export const subscribeAccessTokenChange = (listener: () => void) => {
    window.addEventListener(ACCESS_TOKEN_CHANGE_EVENT, listener);
    window.addEventListener("storage", listener);

    return () => {
        window.removeEventListener(ACCESS_TOKEN_CHANGE_EVENT, listener);
        window.removeEventListener("storage", listener);
    };
};
