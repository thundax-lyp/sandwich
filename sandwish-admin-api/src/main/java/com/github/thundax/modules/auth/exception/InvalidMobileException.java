package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidMobileException
 *
 * @author thundax
 */
public class InvalidMobileException extends ApiException {

    public InvalidMobileException() {
        super(I18nMessages.getMessage("auth.exception.invalid-mobile"));
    }
}
