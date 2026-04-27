package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidWhiteIpException
 *
 * @author thundax
 */
public class InvalidWhiteIpException extends ApiException {

    private static final long serialVersionUID = 1L;

    public InvalidWhiteIpException() {
        super(I18nMessages.getMessage("auth.exception.invalid-ip"));
    }
}
