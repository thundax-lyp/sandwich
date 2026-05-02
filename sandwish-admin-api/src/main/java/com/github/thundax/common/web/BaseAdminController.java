package com.github.thundax.common.web;

import com.github.thundax.common.utils.CookieUtils;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseAdminController {

    protected static final String PARAM_RELOAD = "reload";

    protected static final String ATTR_MESSAGE = "message";

    protected static final String MESSAGE_SUCCESS = "success";
    protected static final String MESSAGE_WARN = "warning";

    protected String modulePath;

    @PostConstruct
    public void initModulePath() {
        Class<?> clazz = getClass();
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            String[] path = requestMapping.path();
            if (path.length == 0) {
                path = requestMapping.value();
            }
            if (path.length > 0) {
                modulePath = path[0];
            }
        }
    }

    protected void addSuccessMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, MESSAGE_SUCCESS + ":" + message);
    }

    protected void addWarningMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, MESSAGE_WARN + ":" + message);
    }

    protected void setupParamsModel(HttpServletRequest request, Model model) {
        request.getParameterMap().forEach((name, values) -> {
            if (StringUtils.isNotBlank(name) && values != null) {
                if (values.length == 1) {
                    model.addAttribute(name, values[0]);
                } else if (values.length > 1) {
                    model.addAttribute(name, values);
                }
            }
        });
    }

    protected static String readReloadString(
            String paramName, String cookieName, HttpServletRequest request, HttpServletResponse response) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null) {
            CookieUtils.setCookie(response, cookieName, paramValue);
            return paramValue;
        } else if (request.getParameter(PARAM_RELOAD) != null) {
            paramValue = CookieUtils.getCookie(request, cookieName);
            return paramValue;
        }
        CookieUtils.setCookie(response, cookieName, StringUtils.EMPTY, 0);
        return null;
    }
}
