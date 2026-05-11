package com.github.thundax.modules.auth.service.provider;

public interface GithubLoginProvider {

    String resolveIdentity(String code);
}
