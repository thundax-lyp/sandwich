package com.github.thundax.modules.assist.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 签名 MyBatis Mapper。 */
@MyBatisDao
public interface SignatureMapper {

    SignatureDO get(SignatureDO entity);

    List<SignatureDO> getMany(@Param("idList") List<String> idList);

    List<SignatureDO> findList(SignatureDO entity);

    int insert(SignatureDO entity);

    int update(SignatureDO entity);

    int delete(SignatureDO entity);

    int insertOrUpdate(SignatureDO entity);
}
