package com.github.thundax.common.security.user;

import org.springframework.security.core.Authentication;

public interface CurrentUserResolver {

    CurrentUser resolve(Authentication authentication);
}
