package com.github.thundax.modules.sys.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.entity.Office;
import java.util.List;

public interface OfficeDao {

    Office get(String id);

    List<Office> getMany(List<String> idList);

    List<Office> findList(String parentId, String name, String remarks);

    Page<Office> findPage(String parentId, String name, String remarks, int pageNo, int pageSize);

    int insert(Office office);

    int update(Office office);

    int updatePriority(Office office);

    int updateDelFlag(Office office);

    int delete(String id);

    void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType);

    boolean isChildOf(String childId, String parentId);
}
