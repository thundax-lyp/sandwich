package com.github.thundax.modules.storage.persistence.mapper;

import com.github.thundax.common.persistence.CrudDao;
import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.storage.persistence.dataobject.StorageBusinessDO;
import com.github.thundax.modules.storage.persistence.dataobject.StorageDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 存储文件 MyBatis Mapper。
 */
@MyBatisDao
public interface StorageMapper extends CrudDao<StorageDO> {

    List<String> findMimeTypeList();

    List<String> findBusinessTypeList();

    int updateEnableFlag(StorageDO storage);

    int updatePublicFlag(StorageDO storage);

    List<StorageBusinessDO> findBusiness(StorageDO entity);

    void insertBusiness(@Param("list") List<StorageBusinessDO> list);

    void deleteBusiness(StorageDO entity);

    int deleteBusinessByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);
}
