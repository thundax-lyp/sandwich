package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author wdit
 */
public class RequireRoleException extends ApiException {

    public RequireRoleException(String role) {
        super(I18nMessages.getMessage("auth.exception.require-role", role));
    }

}
