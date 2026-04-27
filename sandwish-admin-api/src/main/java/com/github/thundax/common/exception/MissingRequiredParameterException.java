package com.github.thundax.common.exception;

import com.github.thundax.common.i18n.I18nMessages;

/** @author wdit */
public class MissingRequiredParameterException extends ApiException {

    public MissingRequiredParameterException(String name) {
        super(I18nMessages.getMessage("common.exception.missing-required-parameter", name));
    }
}
