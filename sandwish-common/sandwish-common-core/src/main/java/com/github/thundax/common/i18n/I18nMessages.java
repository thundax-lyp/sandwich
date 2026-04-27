package com.github.thundax.common.i18n;

import com.github.thundax.common.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.stereotype.Service;

/** @author thundax */
@Service
@Lazy(false)
public class I18nMessages {

    private static MessageSource defaultMessageSource;

    @Autowired
    public I18nMessages(MessageSource messageSource) {
        defaultMessageSource = messageSource;
    }

    private static MessageSource getDefaultMessageSource() {
        if (defaultMessageSource == null) {
            defaultMessageSource = SpringContextHolder.getBean(MessageSource.class);
        }
        return defaultMessageSource;
    }

    public static String getMessage(String code, Object... args) {
        try {
            return getDefaultMessageSource()
                    .getMessage(code, args, LocaleContextHolder.getLocale());

        } catch (NoSuchMessageException e) {
            throw new IllegalArgumentException("undefined message:" + code);
        }
    }

    public static String getMessage(String[] basename, String code, Object... args) {
        try {
            MessageSource messageSource = getDefaultMessageSource();
            if (messageSource instanceof AbstractResourceBasedMessageSource) {
                ((AbstractResourceBasedMessageSource) messageSource).addBasenames(basename);
            }
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());

        } catch (NoSuchMessageException e) {
            throw new IllegalArgumentException("undefined message:" + code);
        }
    }
}
