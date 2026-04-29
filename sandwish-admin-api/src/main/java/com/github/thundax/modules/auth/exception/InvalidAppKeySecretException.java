package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

/**
 * InvalidAppKeySecretException
 *
 * @author thundax
 */
public class InvalidAppKeySecretException extends ApiException {

    public InvalidAppKeySecretException() {
        super(I18nMessages.getMessage("auth.exception.invalid-appKey-appSecret"));
    }
}
