package com.github.thundax.modules.auth.service.provider;

import com.github.thundax.common.exception.ApiException;

public interface GithubLoginProvider {

    String resolveIdentity(String code) throws ApiException;
}
