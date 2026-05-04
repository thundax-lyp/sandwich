package com.github.thundax.modules.auth.service.provider;

import com.github.thundax.common.exception.ApiException;

public interface WecomLoginProvider {

    String resolveIdentity(String code) throws ApiException;
}
