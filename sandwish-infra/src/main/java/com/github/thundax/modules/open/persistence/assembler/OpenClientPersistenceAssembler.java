package com.github.thundax.modules.open.persistence.assembler;

import com.github.thundax.modules.open.entity.OpenClient;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientIdCodec;
import com.github.thundax.modules.open.entity.valueobject.OpenClientPermissionIdCodec;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientDO;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientPermissionDO;
import java.util.ArrayList;
import java.util.List;

public final class OpenClientPersistenceAssembler {

    private OpenClientPersistenceAssembler() {}

    public static OpenClientDO toDataObject(OpenClient entity) {
        if (entity == null) {
            return null;
        }
        OpenClientDO dataObject = new OpenClientDO();
        dataObject.setId(OpenClientIdCodec.toValue(entity.getId()));
        dataObject.setName(entity.getName());
        dataObject.setStatus(statusValue(entity.getStatus()));
        dataObject.setIpWhitelist(entity.getIpWhitelist());
        dataObject.setExpiredAt(entity.getExpiredAt());
        dataObject.setRemarks(entity.getRemarks());
        return dataObject;
    }

    public static OpenClientPermissionDO toDataObject(OpenClientPermission entity) {
        if (entity == null) {
            return null;
        }
        OpenClientPermissionDO dataObject = new OpenClientPermissionDO();
        dataObject.setId(OpenClientPermissionIdCodec.toValue(entity.getId()));
        dataObject.setClientId(OpenClientIdCodec.toValue(entity.getClientId()));
        dataObject.setPermission(entity.getPermission());
        return dataObject;
    }

    public static OpenClient toEntity(OpenClientDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OpenClient entity = new OpenClient();
        entity.setId(OpenClientIdCodec.toDomain(dataObject.getId()));
        entity.setName(dataObject.getName());
        entity.setStatus(statusFrom(dataObject.getStatus()));
        entity.setIpWhitelist(dataObject.getIpWhitelist());
        entity.setExpiredAt(dataObject.getExpiredAt());
        entity.setRemarks(dataObject.getRemarks());
        return entity;
    }

    public static OpenClientPermission toEntity(OpenClientPermissionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        OpenClientPermission entity = new OpenClientPermission();
        entity.setId(OpenClientPermissionIdCodec.toDomain(dataObject.getId()));
        entity.setClientId(OpenClientIdCodec.toDomain(dataObject.getClientId()));
        entity.setPermission(dataObject.getPermission());
        return entity;
    }

    public static List<OpenClient> toEntityList(List<OpenClientDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OpenClient> entities = new ArrayList<>();
        for (OpenClientDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    public static List<OpenClientPermission> toPermissionEntityList(List<OpenClientPermissionDO> dataObjects) {
        if (dataObjects == null) {
            return null;
        }
        List<OpenClientPermission> entities = new ArrayList<>();
        for (OpenClientPermissionDO dataObject : dataObjects) {
            entities.add(toEntity(dataObject));
        }
        return entities;
    }

    private static String statusValue(OpenClientStatus status) {
        return status == null ? null : status.value();
    }

    private static OpenClientStatus statusFrom(String status) {
        return status == null ? null : OpenClientStatus.from(status);
    }
}
