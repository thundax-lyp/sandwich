package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 机构 MyBatis Mapper。 */
@MyBatisDao
public interface OfficeMapper {

    OfficeDO get(OfficeDO office);

    List<OfficeDO> getMany(@Param("idList") List<String> idList);

    List<OfficeDO> findList(OfficeDO office);

    int insert(OfficeDO office);

    int update(OfficeDO office);

    int updatePriority(OfficeDO office);

    int updateStatus(OfficeDO office);

    int updateDelFlag(OfficeDO office);

    int delete(OfficeDO office);

    OfficeDO getTreeNode(String id);

    void updateLftRgt(@Param("node") OfficeDO node);

    void updateParent(@Param("node") OfficeDO node);

    Integer getMaxPosition();

    void moveTreeRgts(@Param("from") Integer from, @Param("offset") Integer offset);

    void moveTreeLfts(@Param("from") Integer from, @Param("offset") Integer offset);

    void moveTreeNodes(
            @Param("from") Integer from, @Param("to") Integer to, @Param("offset") Integer offset);
}
