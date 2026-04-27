package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author wdit */
public class RequiredParameterException extends ApiException {

    public RequiredParameterException(String message) {
        super(I18nMessages.getMessage("common.exception.required-parameter", message));
    }
}
