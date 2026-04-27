package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.response.LogOfficeResponse;
import com.github.thundax.modules.sys.response.LogResponse;
import com.github.thundax.modules.sys.response.LogUserResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LogInterfaceAssembler {

    @NonNull
    public LogResponse toResponse(Log entity) {
        if (entity == null) {
            return new LogResponse();
        }

        LogResponse response = baseEntityToResponse(new LogResponse(), entity);

        response.setType(entity.getType());
        response.setTitle(entity.getTitle());
        response.setRemoteAddr(entity.getRemoteAddr());
        response.setUserAgent(entity.getUserAgent());
        response.setMethod(entity.getMethod());
        response.setRequestUri(entity.getRequestUri());
        response.setRequestParams(entity.getRequestParams());
        response.setCreateDate(entity.getLogDate());
        response.setCreateUser(toUserResponse(entity.getUser()));

        return response;
    }

    @NonNull
    public LogUserResponse toUserResponse(User entity) {
        if (entity == null) {
            return new LogUserResponse();
        }

        LogUserResponse response = new LogUserResponse();
        response.setId(entity.getId());
        response.setLoginName(entity.getLoginName());
        response.setName(entity.getName());
        response.setOffice(toOfficeResponse(entity.getOffice()));
        return response;
    }

    @NonNull
    public LogOfficeResponse toOfficeResponse(Office entity) {
        if (entity == null) {
            return new LogOfficeResponse();
        }

        LogOfficeResponse response = new LogOfficeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setNamePath(entity.getNamePath());
        return response;
    }

    private static LogResponse baseEntityToResponse(LogResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        return response;
    }
}
