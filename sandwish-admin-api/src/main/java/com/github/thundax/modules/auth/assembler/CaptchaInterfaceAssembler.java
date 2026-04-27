package com.github.thundax.modules.auth.assembler;

import com.github.thundax.modules.auth.response.CaptchaRefreshResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CaptchaInterfaceAssembler {

    @NonNull
    public CaptchaRefreshResponse toRefreshResponse(boolean refreshed) {
        CaptchaRefreshResponse response = new CaptchaRefreshResponse();
        response.setRefreshed(refreshed);
        return response;
    }
}
