package com.github.thundax.modules.storage.service;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import java.util.List;

public interface StorageService {

    Storage get(EntityId id);

    List<Storage> getMany(List<String> ids);

    List<Storage> findList(Storage storage);

    Page<Storage> findPage(Storage storage, Page<Storage> page);

    void add(Storage storage);

    void update(Storage storage);

    int delete(Storage storage);

    int delete(List<Storage> list);

    /**
     * 获取MIME列表
     *
     * @return MIME列表
     */
    List<String> findMimeTypeList();

    /**
     * 获取业务类型列表
     *
     * @return 业务类型列表
     */
    List<String> findBusinessTypeList();

    /**
     * 更新状态
     *
     * @param storage 资源
     * @return 影响记录数
     */
    int updateStatus(Storage storage);

    /**
     * 更新可见性
     *
     * @param storage 资源
     * @return 影响记录数
     */
    int updateVisibility(Storage storage);

    /**
     * 按业务删除
     *
     * @param businessType 业务类型
     * @param businessId 业务id
     * @return 影响记录数
     */
    int removeBusiness(String businessType, String businessId);

    /**
     * 插入关系
     *
     * @param list
     */
    void insertBusiness(List<StorageBusiness> list);

    /**
     * 查找关系
     *
     * @param entity
     * @return
     */
    List<StorageBusiness> findBusiness(Storage entity);
}
