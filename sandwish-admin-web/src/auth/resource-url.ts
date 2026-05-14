import { useEffect, useState } from "react";
import { getAccessToken, subscribeAccessTokenChange } from "./token-storage";

export const toAuthenticatedResourceUrl = (
    resourceUrl?: string | null,
    accessToken: string | null = getAccessToken()
) => {
    if (!resourceUrl || !accessToken) {
        return resourceUrl || undefined;
    }

    const url = new URL(resourceUrl, window.location.origin);
    url.searchParams.set("token", accessToken);
    return url.origin === window.location.origin
        ? `${url.pathname}${url.search}${url.hash}`
        : url.toString();
};

export const useCurrentAccessToken = () => {
    const [accessToken, setAccessToken] = useState(() => getAccessToken());

    useEffect(() => {
        return subscribeAccessTokenChange(() => setAccessToken(getAccessToken()));
    }, []);

    return accessToken;
};
