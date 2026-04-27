package com.github.thundax.common.web;

import com.github.thundax.common.utils.DateFormatUtils;
import com.github.thundax.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;

/**
 * @author thundax
 */
public abstract class BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

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
                    java.util.Date value = DateFormatUtils.parseDate(text);
                    if (value == null) {
                        setValue(null);
                    } else {
                        setValue(new java.sql.Timestamp(value.getTime()));
                    }
                }
            }

            @Override
            public String getAsText() {
                Date value = (Date) getValue();
                return (value != null ? DateFormatUtils.format(value) : "");
            }
        });
    }

}
