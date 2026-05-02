package com.github.thundax.modules.sys.controller;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 服务端标签选择视图支撑入口，不承载核心业务 API 规则。
 */
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController {

    @RequestMapping(value = "treeSelector")
    public String treeSelector(HttpServletRequest request, Model model) {
        setupParamsModel(request, model);

        return "modules/sys/tagTreeselect";
    }

    @RequestMapping(value = "iconSelector")
    public String iconSelector(HttpServletRequest request, Model model) {
        setupParamsModel(request, model);

        return "modules/sys/tagIconSelector";
    }

    @RequestMapping(value = "locationSelector")
    public String locationSelector(HttpServletRequest request, Model model) {
        setupParamsModel(request, model);

        model.addAttribute("defaultCity", "");
        model.addAttribute("defaultCenterAddress", "");
        model.addAttribute("defaultZoom", "");

        return "modules/sys/tagLocationSelector";
    }

    @RequestMapping(value = "codeditor")
    public String codeditor(HttpServletRequest request, Model model) {
        setupParamsModel(request, model);

        return "modules/sys/tagCodeditor";
    }

    private void setupParamsModel(HttpServletRequest request, Model model) {
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
}
