package com.github.thundax.modules.storage.dao;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.StorageBusiness;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wdit
 */
public interface StorageDao extends CrudDao<Storage> {

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
    void insertBusiness(@Param("list") List<StorageBusiness> list);

    /**
     * 按业务删除
     *
     * @param entity entity
     */
    void deleteBusiness(Storage entity);

    /**
     * 按业务删除
     *
     * @param businessType 业务类型
     * @param businessId   业务id
     * @return 影响记录数
     */
    int deleteBusinessByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);

}
