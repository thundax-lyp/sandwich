package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.persistence.assembler.MenuPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.MenuCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import com.github.thundax.modules.sys.persistence.dataobject.MenuRoleDO;
import com.github.thundax.modules.sys.persistence.mapper.MenuMapper;
import com.github.thundax.modules.sys.persistence.mapper.MenuRoleMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MenuDaoImpl implements MenuDao {

    private final MenuMapper mapper;
    private final MenuRoleMapper menuRoleMapper;
    private final MenuCacheSupport cacheSupport;

    public MenuDaoImpl(MenuMapper mapper, MenuRoleMapper menuRoleMapper, MenuCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.menuRoleMapper = menuRoleMapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Menu get(String id) {
        Menu menu = cacheSupport.getById(id);
        if (menu != null) {
            return menu;
        }

        menu = MenuPersistenceAssembler.toEntity(mapper.selectById(id));
        cacheSupport.putById(menu);
        return menu;
    }

    @Override
    public List<Menu> getMany(List<String> idList) {
        List<Menu> menuList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Menu menu = cacheSupport.getById(id);
            if (menu == null) {
                uncachedIdList.add(id);
            } else {
                menuList.add(menu);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<Menu> uncachedMenuList = MenuPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Menu menu : uncachedMenuList) {
                cacheSupport.putById(menu);
                menuList.add(menu);
            }
        }
        return menuList;
    }

    @Override
    public List<Menu> findList(String parentId, String displayFlag, Integer maxRank) {
        return MenuPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(parentId, displayFlag, maxRank)));
    }

    @Override
    public Page<Menu> findPage(String parentId, String displayFlag, Integer maxRank, int pageNo, int pageSize) {
        IPage<MenuDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(parentId, displayFlag, maxRank));
        Page<Menu> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(MenuPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Menu entity) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(dataObject.getParentId());
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        moveTreeRgts(newPosition, 2);
        moveTreeLfts(newPosition, 2);
        mapper.insert(dataObject);
        cacheSupport.removeAll();
        return dataObject.getId();
    }

    @Override
    public int update(Menu entity) {
        MenuDO oldNode = getTreeNode(entity.getId());
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(dataObject.getParentId());
        if (oldNode != null && !StringUtils.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MenuDO::getParentId, dataObject.getParentId())
                        .set(MenuDO::getName, dataObject.getName())
                        .set(MenuDO::getPerms, dataObject.getPerms())
                        .set(MenuDO::getRanks, dataObject.getRanks())
                        .set(MenuDO::getUrl, dataObject.getUrl())
                        .set(MenuDO::getTarget, dataObject.getTarget())
                        .set(MenuDO::getDisplayFlag, dataObject.getDisplayFlag())
                        .set(MenuDO::getDisplayParams, dataObject.getDisplayParams())
                        .set(MenuDO::getRemarks, dataObject.getRemarks())
                        .set(MenuDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MenuDO::getUpdateUserId, dataObject.getUpdateUserId())
                        .set(MenuDO::getDelFlag, dataObject.getDelFlag()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Menu entity) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MenuDO::getPriority, dataObject.getPriority())
                        .set(MenuDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MenuDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updateDelFlag(Menu entity) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MenuDO::getDelFlag, dataObject.getDelFlag())
                        .set(MenuDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MenuDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(String id) {
        MenuDO node = getTreeNode(id);
        if (node == null) {
            return 0;
        }
        moveTreeRgts(node.getLft(), -treeSpan(node));
        moveTreeLfts(node.getLft(), -treeSpan(node));
        int count = mapper.deleteById(id);
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType) {
        MenuDO fromNode = getTreeNode(fromId);
        MenuDO toNode = getTreeNode(toId);

        int newPosition;
        String newParentId;
        if (moveType == TreeService.MoveTreeNodeType.AFTER) {
            newPosition = toNode.getRgt() + 1;
            newParentId = toNode.getParentId();
        } else if (moveType == TreeService.MoveTreeNodeType.BEFORE) {
            newPosition = toNode.getLft();
            newParentId = toNode.getParentId();
        } else if (moveType == TreeService.MoveTreeNodeType.INSIDE) {
            newPosition = toNode.getLft() + 1;
            newParentId = toId;
        } else {
            newPosition = toNode.getRgt();
            newParentId = toId;
        }

        moveTreeLfts(newPosition, treeSpan(fromNode));
        moveTreeRgts(newPosition, treeSpan(fromNode));

        fromNode = getTreeNode(fromId);
        int offset = newPosition - fromNode.getLft();
        moveTreeNodes(fromNode.getLft(), fromNode.getRgt(), offset);

        moveTreeLfts(fromNode.getLft(), -treeSpan(fromNode));
        moveTreeRgts(fromNode.getLft(), -treeSpan(fromNode));

        MenuDO parentUpdate = new MenuDO();
        parentUpdate.setId(fromId);
        parentUpdate.setParentId(newParentId);
        updateParent(parentUpdate);
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(String childId, String parentId) {
        MenuDO child = getTreeNode(childId);
        MenuDO parent = getTreeNode(parentId);
        return child != null && parent != null && child.getLft() > parent.getLft() && child.getRgt() < parent.getRgt();
    }

    @Override
    public int updateDisplayFlag(Menu menu) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(menu);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(MenuDO::getDisplayFlag, dataObject.getDisplayFlag())
                        .set(MenuDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(MenuDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(menu.getId());
        return count;
    }

    @Override
    public void deleteMenuRole(String menuId) {
        LambdaQueryWrapper<MenuRoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuRoleDO::getMenuId, menuId);
        menuRoleMapper.delete(wrapper);
    }

    private Integer allocateInsertPosition(MenuDO node) {
        normalizeParentId(node);
        if (StringUtils.isNotBlank(node.getParentId()) && !StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID)) {
            MenuDO parent = getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(MenuDO oldNode, String parentId) {
        Integer newPosition = getInsertPosition(parentId);
        moveTreeRgts(newPosition, treeSpan(oldNode));
        moveTreeLfts(newPosition, treeSpan(oldNode));

        oldNode = getTreeNode(oldNode.getId());
        int offset = newPosition - oldNode.getLft();
        moveTreeNodes(oldNode.getLft(), oldNode.getRgt(), offset);

        moveTreeRgts(oldNode.getLft(), -treeSpan(oldNode));
        moveTreeLfts(oldNode.getLft(), -treeSpan(oldNode));
    }

    private Integer getInsertPosition(String parentId) {
        if (StringUtils.isNotBlank(parentId) && !StringUtils.equals(parentId, TreeEntity.ROOT_ID)) {
            MenuDO parent = getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private MenuDO getTreeNode(String id) {
        return mapper.selectById(id);
    }

    private Integer getMaxPosition() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<MenuDO>().select("MAX(rgt)"));
        if (maxValues == null || maxValues.isEmpty() || maxValues.get(0) == null) {
            return null;
        }
        return ((Number) maxValues.get(0)).intValue();
    }

    private void updateParent(MenuDO node) {
        mapper.update(null, buildIdUpdateWrapper(node).set(MenuDO::getParentId, node.getParentId()));
    }

    private void moveTreeRgts(Integer from, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(MenuDO::getRgt, from).setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeLfts(Integer from, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(MenuDO::getLft, from).setSql("lft = lft + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeNodes(Integer from, Integer to, Integer offset) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(MenuDO::getLft, from, to)
                .setSql("lft = lft + " + offset)
                .setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private LambdaUpdateWrapper<MenuDO> buildIdUpdateWrapper(MenuDO dataObject) {
        LambdaUpdateWrapper<MenuDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MenuDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<MenuDO> buildListWrapper(String parentId, String displayFlag, Integer maxRank) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getDelFlag, MenuDO.DEL_FLAG_NORMAL);
        if (parentId != null) {
            if (StringUtils.equals(parentId, TreeEntity.ROOT_ID)) {
                wrapper.isNull(MenuDO::getParentId);
            } else {
                wrapper.eq(MenuDO::getParentId, parentId);
            }
        }
        if (StringUtils.isNotBlank(displayFlag)) {
            wrapper.eq(MenuDO::getDisplayFlag, displayFlag);
        }
        if (maxRank != null) {
            wrapper.le(MenuDO::getRanks, maxRank);
        }
        wrapper.orderByAsc(MenuDO::getLft);
        return wrapper;
    }

    private static void normalizeParentId(MenuDO node) {
        if (node != null
                && (StringUtils.isBlank(node.getParentId())
                        || StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(MenuDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
