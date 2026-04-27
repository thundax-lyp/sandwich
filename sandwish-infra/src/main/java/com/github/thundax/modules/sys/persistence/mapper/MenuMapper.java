package com.github.thundax.modules.sys.persistence.mapper;

import com.github.thundax.common.persistence.annotation.MyBatisDao;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 菜单 MyBatis Mapper。 */
@MyBatisDao
public interface MenuMapper {

    MenuDO get(MenuDO menu);

    List<MenuDO> getMany(@Param("idList") List<String> idList);

    List<MenuDO> findList(MenuDO menu);

    int insert(MenuDO menu);

    int update(MenuDO menu);

    int updatePriority(MenuDO menu);

    int updateStatus(MenuDO menu);

    int updateDelFlag(MenuDO menu);

    int delete(MenuDO menu);

    MenuDO getTreeNode(String id);

    void updateLftRgt(@Param("node") MenuDO node);

    void updateParent(@Param("node") MenuDO node);

    Integer getMaxPosition();

    void moveTreeRgts(@Param("from") Integer from, @Param("offset") Integer offset);

    void moveTreeLfts(@Param("from") Integer from, @Param("offset") Integer offset);

    void moveTreeNodes(
            @Param("from") Integer from, @Param("to") Integer to, @Param("offset") Integer offset);

    /**
     * 更新 displayFlag。
     *
     * @param menu 菜单
     * @return 影响记录数
     */
    int updateDisplayFlag(MenuDO menu);

    /**
     * 删除菜单权限关系。
     *
     * @param menu 菜单
     */
    void deleteMenuRole(MenuDO menu);
}
