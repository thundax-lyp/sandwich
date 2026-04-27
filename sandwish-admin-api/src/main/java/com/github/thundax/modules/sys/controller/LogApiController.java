package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.assembler.LogInterfaceAssembler;
import com.github.thundax.modules.sys.api.LogServiceApi;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.request.LogPageRequest;
import com.github.thundax.modules.sys.response.LogResponse;
import com.github.thundax.modules.sys.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

/**
 * @author wdit
 */
@RestController
public class LogApiController extends BaseApiController implements LogServiceApi {

    private final LogService logService;
    private final LogInterfaceAssembler logInterfaceAssembler;

    @Autowired
    public LogApiController(LogService logService,
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

        query.setQueryProp(Log.Query.PROP_TITLE, request.getTitle());
        query.setQueryProp(Log.Query.PROP_REMOTE_ADDR, request.getRemoteAddr());
        query.setQueryProp(Log.Query.PROP_REQUEST_URI, request.getRequestUri());

        query.setQueryProp(Log.Query.PROP_USER_LOGIN_NAME, request.getUserLoginName());
        query.setQueryProp(Log.Query.PROP_USER_NAME, request.getUserName());

        query.setQueryProp(Log.Query.PROP_BEGIN_DATE, request.getBeginDate());
        query.setQueryProp(Log.Query.PROP_END_DATE, request.getEndDate());

        return entityPageToVo(logService.findPage(query, readLogPage(request)), logInterfaceAssembler::toResponse);
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
