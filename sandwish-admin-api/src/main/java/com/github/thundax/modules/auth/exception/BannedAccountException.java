package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * BannedAccountException
 *
 * @author wdit
 */
public class BannedAccountException extends ApiException {

    private static final long serialVersionUID = 1L;

    public BannedAccountException() {
        super(I18nMessages.getMessage("auth.exception.banned-account"));
    }
}
