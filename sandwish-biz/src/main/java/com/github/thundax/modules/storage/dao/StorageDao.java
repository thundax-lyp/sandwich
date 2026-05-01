package com.github.thundax.modules.storage.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import java.util.List;

public interface StorageDao {

    Storage getById(EntityId id);

    List<Storage> batchGetByIds(List<String> idList);

    List<Storage> list(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String name,
            String remarks);

    Page<Storage> page(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String publicFlag,
            String name,
            String remarks,
            int pageNo,
            int pageSize);

    String insert(Storage entity);

    int update(Storage entity);

    int deleteById(EntityId id);

    /**
     * 获取MIME列表
     *
     * @return MIME列表
     */
    List<String> listMimeTypes();

    /**
     * 获取业务类型列表
     *
     * @return 业务类型列表
     */
    List<String> listBusinessTypes();

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
     * 读取
     *
     * @param entity entity
     * @return 列表
     */
    List<StorageBusiness> listBusiness(Storage entity);

    /**
     * 写入
     *
     * @param list list
     */
    void insertBusiness(List<StorageBusiness> list);

    /**
     * 按业务删除
     *
     * @param id 文件 id
     */
    void deleteBusiness(String id);

    /**
     * 按业务删除
     *
     * @param businessType 业务类型
     * @param businessId 业务id
     * @return 影响记录数
     */
    int deleteBusinessByBusiness(String businessType, String businessId);
}
