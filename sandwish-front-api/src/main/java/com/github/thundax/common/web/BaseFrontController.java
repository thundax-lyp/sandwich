package com.github.thundax.common.web;

import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageRules;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class BaseFrontController {

    protected static final String ATTR_MESSAGE = "message";

    protected static final String MESSAGE_SUCCESS = "success";
    protected static final String MESSAGE_WARN = "warning";
    protected static final String MESSAGE_ERROR = "error";

    protected Logger logger = LoggerFactory.getLogger(getClass());

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

    protected void addMessage(@NotNull Model model, String message) {
        if (model != null) {
            model.addAttribute(ATTR_MESSAGE, message);
        }
    }

    protected void addMessage(@NotNull RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(ATTR_MESSAGE, message);
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

    public static PageQuery readPage(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < PageRules.firstPageIndex()) {
            pageNo = PageRules.firstPageIndex();
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = PageRules.defaultPageSize();
        }

        PageQuery page = new PageQuery();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
