package com.github.thundax.modules.auth.assembler;

import com.github.thundax.modules.auth.controller.response.CaptchaRefreshResponse;
import org.springframework.lang.NonNull;

public final class CaptchaInterfaceAssembler {
    private CaptchaInterfaceAssembler() {}

    @NonNull
    public static CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        return CaptchaRefreshResponse.builder().refreshed(refreshed).build();
    }
}
