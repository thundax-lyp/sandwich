package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidMobileException
 *
 * @author thundax
 */
public class InvalidMobileException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidMobileException() {
        super(I18nMessages.getMessage("auth.exception.invalid-mobile"));
    }
}
