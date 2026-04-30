package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.response.LogOfficeResponse;
import com.github.thundax.modules.sys.response.LogResponse;
import com.github.thundax.modules.sys.response.LogUserResponse;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LogInterfaceAssembler {

    private final UserService userService;
    private final OfficeService officeService;

    public LogInterfaceAssembler(UserService userService, OfficeService officeService) {
        this.userService = userService;
        this.officeService = officeService;
    }

    @NonNull
    public LogResponse toResponse(Log entity) {
        if (entity == null) {
            return new LogResponse();
        }

        LogResponse response = baseEntityToResponse(new LogResponse(), entity);

        response.setType(entity.getType() == null ? null : entity.getType().value());
        response.setTitle(entity.getTitle());
        response.setRemoteAddr(entity.getRemoteAddr());
        response.setUserAgent(entity.getUserAgent());
        response.setMethod(entity.getMethod());
        response.setRequestUri(entity.getRequestUri());
        response.setRequestParams(entity.getRequestParams());
        response.setCreateDate(entity.getLogDate());
        response.setCreateUser(toUserResponse(userService.get(EntityIdCodec.toDomain(entity.getUserId()))));

        return response;
    }

    @NonNull
    public LogUserResponse toUserResponse(User entity) {
        if (entity == null) {
            return new LogUserResponse();
        }

        LogUserResponse response = new LogUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setLoginName(entity.getLoginName());
        response.setName(entity.getName());
        response.setOffice(toOfficeResponse(officeService.get(EntityIdCodec.toDomain(entity.getOfficeId()))));
        return response;
    }

    @NonNull
    public LogOfficeResponse toOfficeResponse(Office entity) {
        if (entity == null) {
            return new LogOfficeResponse();
        }

        LogOfficeResponse response = new LogOfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity));
        return response;
    }

    private static LogResponse baseEntityToResponse(LogResponse response, Log entity) {
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        return response;
    }

    private String namePath(Office office) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeService.get(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeService.get(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
