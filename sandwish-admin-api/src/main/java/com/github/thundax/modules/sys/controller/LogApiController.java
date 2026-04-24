package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.sys.api.LogServiceApi;
import com.github.thundax.modules.sys.api.query.LogQueryParam;
import com.github.thundax.modules.sys.api.vo.LogVo;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

/**
 * @author wdit
 */
@RestController
public class LogApiController extends BaseApiController implements LogServiceApi {

    private final LogService logService;

    @Autowired
    public LogApiController(LogService logService, Validator validator) {
        super(validator);
        this.logService = logService;
    }


    @Override
//    @RequiresPermissions("super")
    public PageVo<LogVo> page(@RequestBody LogQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Log query = new Log();

        query.setQueryProp(Log.Query.PROP_TITLE, queryParam.getTitle());
        query.setQueryProp(Log.Query.PROP_REMOTE_ADDR, queryParam.getRemoteAddr());
        query.setQueryProp(Log.Query.PROP_REQUEST_URI, queryParam.getRequestUri());

        query.setQueryProp(Log.Query.PROP_USER_LOGIN_NAME, queryParam.getUserLoginName());
        query.setQueryProp(Log.Query.PROP_USER_NAME, queryParam.getUserName());

        query.setQueryProp(Log.Query.PROP_BEGIN_DATE, queryParam.getBeginDate());
        query.setQueryProp(Log.Query.PROP_END_DATE, queryParam.getEndDate());

        return entityPageToVo(logService.findPage(query, readPage(queryParam)), this::entityToVo);
    }


    @NonNull
    private LogVo entityToVo(Log entity) {
        if (entity == null) {
            return new LogVo();
        }

        LogVo vo = baseEntityToVo(new LogVo(), entity);

        vo.setType(entity.getType());
        vo.setTitle(entity.getTitle());
        vo.setRemoteAddr(entity.getRemoteAddr());
        vo.setUserAgent(entity.getUserAgent());
        vo.setMethod(entity.getMethod());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestParams(entity.getRequestParams());

        vo.setCreateDate(entity.getLogDate());

        vo.setCreateUser(entityToVo(entity.getUser()));

        return vo;
    }

    @NonNull
    private UserVo entityToVo(User entity) {
        if (entity == null) {
            return new UserVo();
        }

        UserVo vo = new UserVo(entity.getId());
        vo.setLoginName(entity.getLoginName());
        vo.setName(entity.getName());

        vo.setOffice(entityToVo(entity.getOffice()));

        return vo;
    }

    @NonNull
    private OfficeVo entityToVo(Office entity) {
        if (entity == null) {
            return new OfficeVo();
        }

        OfficeVo vo = new OfficeVo(entity.getId());
        vo.setName(entity.getName());
        vo.setNamePath(entity.getNamePath());

        return vo;
    }

}
