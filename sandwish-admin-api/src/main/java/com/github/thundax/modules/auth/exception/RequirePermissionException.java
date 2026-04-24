package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * @author thundax
 */
public class RequirePermissionException extends ApiException {

    public RequirePermissionException(String permission) {
        super(I18nMessages.getMessage("auth.exception.require-permission", permission));
    }

}
