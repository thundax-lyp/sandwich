package com.github.thundax.common.security.user;

public class AnonymousCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser currentUser() {
        return CurrentUser.anonymous();
    }
}
