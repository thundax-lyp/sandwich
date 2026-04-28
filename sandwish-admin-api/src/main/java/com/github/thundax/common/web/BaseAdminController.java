package com.github.thundax.common.web;

import com.github.thundax.common.exception.WebMvcException;
import com.github.thundax.common.utils.CookieUtils;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.entity.User;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
import org.springframework.lang.NonNull;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseAdminController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

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

    public BaseAdminController(Validator validator) {
        this.validator = validator;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return UserAccessHolder.currentUser();
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

    @ModelAttribute("modulePath")
    public String getModulePath() {
        return this.modulePath;
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

    protected void addSuccessMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, MESSAGE_SUCCESS + ":" + message);
    }

    protected void addWarningMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, MESSAGE_WARN + ":" + message);
    }

    protected void addErrorMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, MESSAGE_ERROR + ":" + message);
    }

    /**
     * 移除树形结构的某分支
     *
     * @param dataList 存放树形结构数据列表，数据结构存放在map中
     * @param keyNameId id在map中的key名
     * @param keyNamePid pid在map中的key名
     * @param extId 要排除的id
     * @return 排除的数据结构数目
     */
    protected List<Map<String, String>> removeTreeNode(
            List<Map<String, String>> dataList, String keyNameId, String keyNamePid, String rootId, String extId) {
        // 创建id-data映射表，用于快速查询pid对应的数据
        Set<String> ids = new HashSet<>();
        for (Map<String, String> data : dataList) {
            String id = data.get(keyNameId);
            ids.add(id);
        }

        boolean loop;
        do {
            loop = false;
            Iterator<Map<String, String>> itor = dataList.iterator();
            while (itor.hasNext()) {
                Map<String, String> data = itor.next();
                String id = data.get(keyNameId);
                String pId = data.get(keyNamePid);
                if (id != null && StringUtils.equals(id, extId)) {
                    // 如果id与extId相同则移除
                    itor.remove();
                    ids.remove(id);
                    loop = true;
                } else if (pId != null && !StringUtils.equals(pId, rootId) && !ids.contains(pId)) {
                    // 如果存在pId且pId对应的数据不存在，则移除
                    itor.remove();
                    ids.remove(id);
                    loop = true;
                }
            }
        } while (loop);

        return dataList;
    }

    /** 将requestParams装配到model */
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

    @NonNull
    protected <T> List<T> validateList(Integer[] ids, @NonNull Function<Integer, T> mappingFunction)
            throws WebMvcException {
        if (ids == null || ids.length == 0) {
            throw new WebMvcException("参数不能为空。");
        }

        List<T> list = new ArrayList<>();

        for (int idx = 0; idx < ids.length; idx++) {
            T entity = mappingFunction.apply(idx);
            if (entity == null) {
                throw new WebMvcException("找不到数据。id:" + ids[idx]);
            }
            list.add(entity);
        }

        return list;
    }
}
