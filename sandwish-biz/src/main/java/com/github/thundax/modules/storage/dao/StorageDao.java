package com.github.thundax.modules.storage.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import java.util.List;

/** @author wdit */
public interface StorageDao {

    Storage get(String id);

    List<Storage> getMany(List<String> idList);

    List<Storage> findList(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String name,
            String remarks);

    Page<Storage> findPage(
            String mimeType,
            String ownerId,
            String ownerType,
            String enableFlag,
            String name,
            String remarks,
            int pageNo,
            int pageSize);

    int insert(Storage entity);

    int update(Storage entity);

    int delete(String id);

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
     * 启用/禁用
     *
     * @param storage 资源
     * @return 影响记录数
     */
    int updateEnableFlag(Storage storage);

    /**
     * 更新发布标记
     *
     * @param storage 资源
     * @return 影响记录数
     */
    int updatePublicFlag(Storage storage);

    /**
     * 读取
     *
     * @param entity entity
     * @return 列表
     */
    List<StorageBusiness> findBusiness(Storage entity);

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
