package com.github.thundax.modules.auth.exception;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.i18n.I18nMessages;

public class InvalidCaptchaException extends ApiException {

    public InvalidCaptchaException() {
        super(I18nMessages.getMessage("auth.exception.invalid-captcha"));
    }
}
