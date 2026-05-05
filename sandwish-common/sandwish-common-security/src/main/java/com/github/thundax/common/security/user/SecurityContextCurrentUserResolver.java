package com.github.thundax.common.security.user;

import com.github.thundax.common.security.permission.PermissionAuthorities;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityContextCurrentUserResolver implements CurrentUserResolver {

    @Override
    public CurrentUser resolve(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return CurrentUser.anonymous();
        }

        CurrentUser currentUser = new CurrentUser();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUser) {
            currentUser = (CurrentUser) principal;
        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            currentUser.setUserId(userDetails.getUsername());
            currentUser.setLoginName(userDetails.getUsername());
            currentUser.setDisplayName(userDetails.getUsername());
        } else if (principal instanceof String) {
            currentUser.setUserId((String) principal);
            currentUser.setLoginName((String) principal);
            currentUser.setDisplayName((String) principal);
        } else {
            currentUser.setUserId(authentication.getName());
            currentUser.setLoginName(authentication.getName());
            currentUser.setDisplayName(authentication.getName());
        }

        currentUser.setAuthorities(PermissionAuthorities.toPermissions(authentication.getAuthorities()));
        return currentUser;
    }
}
