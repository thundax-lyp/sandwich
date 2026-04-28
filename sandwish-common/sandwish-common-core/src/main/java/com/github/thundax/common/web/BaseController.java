package com.github.thundax.common.web;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public abstract class BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String SHORT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String YEAR_PATTERN = "yyyy";
    private static final String MONTH_PATTERN = "yyyy-MM";
    private static final int SHORT_DATE_LENGTH = 10;
    private static final int YEAR_LENGTH = 4;
    private static final int MONTH_LENGTH = 7;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {

        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                text = text.trim();
                if (StringUtils.isBlank(text)) {
                    setValue(null);
                } else {
                    Date value = parseDate(text);
                    if (value == null) {
                        setValue(null);
                    } else {
                        setValue(new Timestamp(value.getTime()));
                    }
                }
            }

            @Override
            public String getAsText() {
                Date value = (Date) getValue();
                return (value != null ? new SimpleDateFormat(DATE_TIME_PATTERN).format(value) : "");
            }
        });
    }

    private static Date parseDate(String text) {
        text = text.trim();
        try {
            if (text.length() <= YEAR_LENGTH) {
                return new SimpleDateFormat(YEAR_PATTERN).parse(text);
            } else if (text.length() <= MONTH_LENGTH) {
                return new SimpleDateFormat(MONTH_PATTERN).parse(text);
            } else if (text.length() <= SHORT_DATE_LENGTH) {
                return new SimpleDateFormat(SHORT_DATE_PATTERN).parse(text);
            }
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(text);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse date: " + e.getMessage(), e);
        }
    }
}
