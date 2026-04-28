package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.web.BaseAdminController;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 服务端标签选择视图支撑入口，不承载核心业务 API 规则。
 *
 * @author wdit
 */
@Controller
@RequestMapping(value = "/admin/tag")
public class TagController extends BaseAdminController {

    @Autowired
    public TagController(Validator validator) {
        super(validator);
    }

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
}
