package com.github.thundax.modules.open.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.SnowflakeIdGenerator;
import com.github.thundax.modules.open.dao.OpenClientDao;
import com.github.thundax.modules.open.entity.OpenClient;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.entity.valueobject.OpenClientIdCodec;
import com.github.thundax.modules.open.persistence.assembler.OpenClientPersistenceAssembler;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientDO;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientPermissionDO;
import com.github.thundax.modules.open.persistence.mapper.OpenClientMapper;
import com.github.thundax.modules.open.persistence.mapper.OpenClientPermissionMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class OpenClientDaoImpl implements OpenClientDao {

    private final OpenClientMapper mapper;
    private final OpenClientPermissionMapper permissionMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public OpenClientDaoImpl(OpenClientMapper mapper, OpenClientPermissionMapper permissionMapper) {
        this.mapper = mapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public OpenClient getById(OpenClientId id) {
        return OpenClientPersistenceAssembler.toEntity(mapper.selectById(OpenClientIdCodec.toValue(id)));
    }

    @Override
    public List<OpenClientPermission> listPermissionsByClientId(OpenClientId clientId) {
        LambdaQueryWrapper<OpenClientPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenClientPermissionDO::getClientId, OpenClientIdCodec.toValue(clientId));
        wrapper.orderByAsc(OpenClientPermissionDO::getPermission);
        wrapper.orderByAsc(OpenClientPermissionDO::getId);
        return OpenClientPersistenceAssembler.toPermissionEntityList(permissionMapper.selectList(wrapper));
    }

    @Override
    public Page<OpenClient> page(String name, String status, int pageNo, int pageSize) {
        Page<OpenClientDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildPageWrapper(name, status));
        Page<OpenClient> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(OpenClientPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public OpenClientId insert(OpenClient entity) {
        OpenClientDO dataObject = OpenClientPersistenceAssembler.toDataObject(entity);
        dataObject.setId(idGenerator.nextId().value());
        mapper.insert(dataObject);
        return OpenClientIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int update(OpenClient entity) {
        OpenClientDO dataObject = OpenClientPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OpenClientDO::getName, dataObject.getName())
                        .set(OpenClientDO::getStatus, dataObject.getStatus())
                        .set(OpenClientDO::getIpWhitelist, dataObject.getIpWhitelist())
                        .set(OpenClientDO::getExpiredAt, dataObject.getExpiredAt())
                        .set(OpenClientDO::getRemarks, dataObject.getRemarks()));
    }

    @Override
    public int updateStatus(OpenClient entity) {
        OpenClientDO dataObject = OpenClientPersistenceAssembler.toDataObject(entity);
        return mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(OpenClientDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public int deleteByClientId(OpenClientId clientId) {
        LambdaQueryWrapper<OpenClientPermissionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpenClientPermissionDO::getClientId, OpenClientIdCodec.toValue(clientId));
        return permissionMapper.delete(wrapper);
    }

    @Override
    public int batchInsertPermissions(List<OpenClientPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (OpenClientPermission permission : permissions) {
            OpenClientPermissionDO dataObject = OpenClientPersistenceAssembler.toDataObject(permission);
            dataObject.setId(idGenerator.nextId().value());
            count += permissionMapper.insert(dataObject);
        }
        return count;
    }

    private LambdaQueryWrapper<OpenClientDO> buildPageWrapper(String name, String status) {
        LambdaQueryWrapper<OpenClientDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(OpenClientDO::getName, name.trim());
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(OpenClientDO::getStatus, status);
        }
        wrapper.orderByDesc(OpenClientDO::getId);
        return wrapper;
    }

    private LambdaUpdateWrapper<OpenClientDO> buildIdUpdateWrapper(OpenClientDO dataObject) {
        LambdaUpdateWrapper<OpenClientDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OpenClientDO::getId, dataObject.getId());
        return wrapper;
    }
}
