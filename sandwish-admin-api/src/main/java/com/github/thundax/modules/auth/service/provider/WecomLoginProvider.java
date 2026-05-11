package com.github.thundax.modules.auth.service.provider;

public interface WecomLoginProvider {

    String resolveIdentity(String code);
}
