package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.api.LogServiceApi;
import com.github.thundax.modules.sys.assembler.LogInterfaceAssembler;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.request.LogPageRequest;
import com.github.thundax.modules.sys.response.LogResponse;
import com.github.thundax.modules.sys.service.LogService;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author wdit */
@RestController
public class LogApiController extends BaseApiController implements LogServiceApi {

    private final LogService logService;
    private final LogInterfaceAssembler logInterfaceAssembler;

    @Autowired
    public LogApiController(
            LogService logService,
            Validator validator,
            LogInterfaceAssembler logInterfaceAssembler) {
        super(validator);
        this.logService = logService;
        this.logInterfaceAssembler = logInterfaceAssembler;
    }

    @Override
    //    @RequiresPermissions("super")
    public PageVo<LogResponse> page(@RequestBody LogPageRequest request) throws ApiException {
        validate(request);

        Log query = new Log();
        Log.Query queryCondition = new Log.Query();

        queryCondition.setTitle(request.getTitle());
        queryCondition.setRemoteAddr(request.getRemoteAddr());
        queryCondition.setRequestUri(request.getRequestUri());

        queryCondition.setUserLoginName(request.getUserLoginName());
        queryCondition.setUserName(request.getUserName());

        queryCondition.setBeginDate(request.getBeginDate());
        queryCondition.setEndDate(request.getEndDate());
        query.setQuery(queryCondition);

        return entityPageToVo(
                logService.findPage(query, readLogPage(request)),
                logInterfaceAssembler::toResponse);
    }

    private Page<Log> readLogPage(LogPageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<Log> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
