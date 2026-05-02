package com.github.thundax.modules.sys.assembler;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.response.LogOfficeResponse;
import com.github.thundax.modules.sys.response.LogResponse;
import com.github.thundax.modules.sys.response.LogUserResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class LogInterfaceAssembler {
    private LogInterfaceAssembler() {}

    @NonNull
    public static LogResponse toResponse(
            Log entity, User user, Office office, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new LogResponse();
        }

        LogResponse response = new LogResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setType(entity.getType() == null ? null : entity.getType().value());
        response.setTitle(entity.getTitle());
        response.setRemoteAddr(entity.getRemoteAddr());
        response.setUserAgent(entity.getUserAgent());
        response.setMethod(entity.getMethod());
        response.setRequestUri(entity.getRequestUri());
        response.setRequestParams(entity.getRequestParams());
        response.setCreateDate(entity.getLogDate());
        response.setCreateUser(toUserResponse(user, office, officeLoader));
        return response;
    }

    @NonNull
    private static LogUserResponse toUserResponse(User entity, Office office, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new LogUserResponse();
        }

        LogUserResponse response = new LogUserResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setLoginName(entity.getLoginName());
        response.setName(entity.getName());
        response.setOffice(toOfficeResponse(office, officeLoader));
        return response;
    }

    @NonNull
    private static LogOfficeResponse toOfficeResponse(Office entity, Function<EntityId, Office> officeLoader) {
        if (entity == null) {
            return new LogOfficeResponse();
        }

        LogOfficeResponse response = new LogOfficeResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setName(entity.getName());
        response.setNamePath(namePath(entity, officeLoader));
        return response;
    }

    private static String namePath(Office office, Function<EntityId, Office> officeLoader) {
        List<String> names = new ArrayList<>();
        Office node = office;
        while (node != null && EntityIdCodec.toValue(node.getId()) != null) {
            node = officeLoader.apply(node.getId());
            if (node != null) {
                names.add(0, node.getName());
                node = officeLoader.apply(EntityIdCodec.toDomain(node.getParentId()));
            }
        }
        return StringUtils.join(names, "/");
    }
}
