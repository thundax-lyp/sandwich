package com.github.thundax.modules.auth.assembler;

import com.github.thundax.modules.auth.response.CaptchaRefreshResponse;
import org.springframework.lang.NonNull;

public final class CaptchaInterfaceAssembler {
    private CaptchaInterfaceAssembler() {}

    @NonNull
    public static CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        CaptchaRefreshResponse response = new CaptchaRefreshResponse();
        response.setRefreshed(refreshed);
        return response;
    }
}
