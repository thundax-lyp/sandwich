package com.github.thundax.common.web;

import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.CookieUtils;
import com.github.thundax.common.vo.PageVo;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseFrontController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public static final int DEFAULT_PAGE_SIZE = 10;

    protected static final String PARAM_RELOAD = "reload";

    protected static final String ATTR_MESSAGE = "message";

    protected static final String MESSAGE_SUCCESS = "success";
    protected static final String MESSAGE_WARN = "warning";
    protected static final String MESSAGE_ERROR = "error";

    private static final String[] DATE_PARSE_PATTERNS = {
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM",
        "yyyy/MM/dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd HH:mm",
        "yyyy/MM",
        "yyyy.MM.dd",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd HH:mm",
        "yyyy.MM"
    };

    protected String modulePath;

    protected Validator validator;

    public BaseFrontController() {}

    public BaseFrontController(Validator validator) {
        this.validator = validator;
    }

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

    //    @ModelAttribute("modulePath")
    public String getModulePath() {
        return this.modulePath;
    }

    public <T> Page<T> readPage(HttpServletRequest request, HttpServletResponse response) {
        Page<T> page = new Page<>(request, response);
        page.setPageSize(DEFAULT_PAGE_SIZE);
        return page;
    }

    protected void addMessage(@NotNull Model model, String message) {
        if (model != null) {
            model.addAttribute(ATTR_MESSAGE, message);
        }
    }

    protected void addMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, message);
    }

    /** 从request中读取整数，如果读取失败且设置了reload开关，则从cookie中获取，读取结果content保存在cookie中 */
    protected static Integer readReloadInteger(
            String paramName, HttpServletRequest request, HttpServletResponse response) {
        return readReloadInteger(paramName, paramName, request, response);
    }

    protected static Integer readReloadInteger(
            String paramName, String cookieName, HttpServletRequest request, HttpServletResponse response) {
        String paramValue = request.getParameter(paramName);
        if (StringUtils.isNumeric(paramValue)) {
            CookieUtils.setCookie(response, cookieName, paramValue);
            return Integer.parseInt(paramValue);

        } else if (request.getParameter(PARAM_RELOAD) != null) {
            paramValue = CookieUtils.getCookie(request, cookieName);
            if (StringUtils.isNumeric(paramValue)) {
                return Integer.parseInt(paramValue);
            }
        }
        CookieUtils.setCookie(response, cookieName, StringUtils.EMPTY, 0);
        return null;
    }

    protected static Date readReloadDate(String paramName, HttpServletRequest request, HttpServletResponse response) {
        return readReloadDate(paramName, paramName, request, response);
    }

    protected static Date readReloadDate(
            String paramName, String cookieName, HttpServletRequest request, HttpServletResponse response) {
        String paramValue = request.getParameter(paramName);
        if (StringUtils.isNotEmpty(paramValue)) {
            CookieUtils.setCookie(response, cookieName, paramValue);
            return parseReloadDateValue(paramValue);

        } else if (request.getParameter(PARAM_RELOAD) != null) {
            paramValue = CookieUtils.getCookie(request, cookieName);
            if (StringUtils.isNotEmpty(paramValue)) {
                return parseReloadDateValue(paramValue);
            }
        }
        CookieUtils.setCookie(response, cookieName, StringUtils.EMPTY, 0);
        return null;
    }

    private static Date parseReloadDateValue(String value) {
        try {
            return DateUtils.parseDate(value, DATE_PARSE_PATTERNS);
        } catch (ParseException e) {
            return null;
        }
    }

    protected static String readReloadString(
            String paramName, HttpServletRequest request, HttpServletResponse response) {
        return readReloadString(paramName, paramName, request, response);
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

    protected <T> boolean validate(RedirectAttributes redirectAttributes, T object) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);
        if (constraintViolations.size() > 0) {
            addWarningMessage(
                    redirectAttributes, constraintViolations.iterator().next().getMessage());
            return false;
        }

        return true;
    }

    protected <T> boolean validate(RedirectAttributes redirectAttributes, T object, String... propertyNames) {
        for (String propertyName : propertyNames) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validateProperty(object, propertyName);
            if (constraintViolations.size() > 0) {
                addWarningMessage(
                        redirectAttributes,
                        constraintViolations.iterator().next().getMessage());
                return false;
            }
        }
        return true;
    }

    protected <T> boolean validate(Model model, T object) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object);
        if (constraintViolations.size() > 0) {
            addWarningMessage(
                    model, "warning:" + constraintViolations.iterator().next().getMessage());
            return false;
        }

        return true;
    }

    protected <T> boolean validate(Model model, T object, String... propertyNames) {
        for (String propertyName : propertyNames) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validateProperty(object, propertyName);
            if (constraintViolations.size() > 0) {
                addWarningMessage(model, constraintViolations.iterator().next().getMessage());
                return false;
            }
        }
        return true;
    }

    protected void addSuccessMessage(@NotNull Model model, String message) {
        if (model != null) {
            model.addAttribute(ATTR_MESSAGE, MESSAGE_SUCCESS + ":" + message);
        }
    }

    protected void addWarningMessage(@NotNull Model model, String message) {
        if (model != null) {
            model.addAttribute(ATTR_MESSAGE, MESSAGE_WARN + ":" + message);
        }
    }

    protected void addErrorMessage(@NotNull Model model, String message) {
        if (model != null) {
            model.addAttribute(ATTR_MESSAGE, MESSAGE_ERROR + ":" + message);
        }
    }

    public static <T, R> PageVo<R> entityPageToVo(Page<T> page, Function<T, R> mappingFunction) {
        PageVo<R> pageVo = new PageVo<>();

        pageVo.setPageNo(page.getPageNo());
        pageVo.setPageSize(page.getPageSize());
        pageVo.setTotalPage(page.getTotalPage());
        pageVo.setCount(page.getCount());

        pageVo.setRecords(
                page.getList() == null
                        ? new ArrayList<>()
                        : page.getList().stream()
                                .map(item -> mappingFunction.apply(item))
                                .collect(Collectors.toList()));

        return pageVo;
    }

    public static <T> Page<T> readPage(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<T> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
