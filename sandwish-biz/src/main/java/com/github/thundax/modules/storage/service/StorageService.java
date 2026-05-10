package com.github.thundax.modules.storage.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.StoredObjectReference;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import com.github.thundax.modules.storage.service.command.AddStorageReferencesCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageObjectStatusCommand;
import com.github.thundax.modules.storage.service.command.ChangeStorageReferenceStatusCommand;
import com.github.thundax.modules.storage.service.command.CreateStorageCommand;
import com.github.thundax.modules.storage.service.command.DeleteStorageCommand;
import com.github.thundax.modules.storage.service.command.RemoveStorageReferencesCommand;
import com.github.thundax.modules.storage.service.query.StorageQuery;
import java.util.List;

public interface StorageService {

    StoredObject get(StoredObjectId id);

    List<StoredObject> list(StorageQuery query);

    PageResult<StoredObject> page(StorageQuery query, PageQuery page);

    StoredObjectId create(CreateStorageCommand command);

    void change(ChangeStorageCommand command);

    int remove(DeleteStorageCommand command);

    List<String> listMimeTypes(StorageQuery query);

    List<String> listReferenceOwnerTypes(StorageQuery query);

    int changeObjectStatus(ChangeStorageObjectStatusCommand command);

    int changeReferenceStatus(ChangeStorageReferenceStatusCommand command);

    @LayerPublicApi(reason = "业务对象删除或解绑时清理存储引用关系的跨模块入口")
    int removeReferences(RemoveStorageReferencesCommand command);

    @LayerPublicApi(reason = "业务对象保存文件后写入存储引用关系的跨模块入口")
    void addReferences(AddStorageReferencesCommand command);

    List<StoredObjectReference> listReferences(StorageQuery query);

    boolean existsReadableContent(StorageQuery query);

    void sort(List<StoredObjectId> orderedIds, SortDirection sortDirection) throws ApiException;
}
