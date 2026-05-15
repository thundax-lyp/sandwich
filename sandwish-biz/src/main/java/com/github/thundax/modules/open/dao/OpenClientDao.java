package com.github.thundax.modules.open.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.open.entity.OpenClient;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import java.util.List;

public interface OpenClientDao {

    OpenClient getById(OpenClientId id);

    List<OpenClientPermission> listPermissionsByClientId(OpenClientId clientId);

    Page<OpenClient> page(String name, String status, int pageNo, int pageSize);

    OpenClientId insert(OpenClient entity);

    int update(OpenClient entity);

    int updateStatus(OpenClient entity);

    int deleteByClientId(OpenClientId clientId);

    int batchInsertPermissions(List<OpenClientPermission> permissions);
}
