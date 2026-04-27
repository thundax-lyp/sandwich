package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.persistence.assembler.MenuPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import com.github.thundax.modules.sys.persistence.mapper.MenuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单 DAO 实现。
 */
@Repository
public class MenuDaoImpl implements MenuDao {

    private final MenuMapper mapper;

    public MenuDaoImpl(MenuMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Menu get(Menu entity) {
        return MenuPersistenceAssembler.toEntity(mapper.get(MenuPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Menu> getMany(List<String> idList) {
        return MenuPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Menu> findList(Menu entity) {
        List<MenuDO> dataObjects = mapper.findList(MenuPersistenceAssembler.toDataObject(entity));
        List<Menu> entities = MenuPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Menu entity) {
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(dataObject.getParentId());
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        mapper.moveTreeRgts(newPosition, 2);
        mapper.moveTreeLfts(newPosition, 2);
        return mapper.insert(dataObject);
    }

    @Override
    public int update(Menu entity) {
        MenuDO oldNode = mapper.getTreeNode(entity.getId());
        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(dataObject.getParentId());
        if (oldNode != null && !StringUtils.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        return mapper.update(dataObject);
    }

    @Override
    public int updatePriority(Menu entity) {
        return mapper.updatePriority(MenuPersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Menu entity) {
        return mapper.updateStatus(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Menu entity) {
        return mapper.updateDelFlag(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Menu entity) {
        MenuDO node = mapper.getTreeNode(entity.getId());
        if (node == null) {
            return 0;
        }
        mapper.moveTreeRgts(node.getLft(), -treeSpan(node));
        mapper.moveTreeLfts(node.getLft(), -treeSpan(node));
        return mapper.delete(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType) {
        MenuDO fromNode = mapper.getTreeNode(fromId);
        MenuDO toNode = mapper.getTreeNode(toId);

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

        mapper.moveTreeLfts(newPosition, treeSpan(fromNode));
        mapper.moveTreeRgts(newPosition, treeSpan(fromNode));

        fromNode = mapper.getTreeNode(fromId);
        int offset = newPosition - fromNode.getLft();
        mapper.moveTreeNodes(fromNode.getLft(), fromNode.getRgt(), offset);

        mapper.moveTreeLfts(fromNode.getLft(), -treeSpan(fromNode));
        mapper.moveTreeRgts(fromNode.getLft(), -treeSpan(fromNode));

        MenuDO parentUpdate = new MenuDO();
        parentUpdate.setId(fromId);
        parentUpdate.setParentId(newParentId);
        mapper.updateParent(parentUpdate);
    }

    @Override
    public boolean isChildOf(String childId, String parentId) {
        MenuDO child = mapper.getTreeNode(childId);
        MenuDO parent = mapper.getTreeNode(parentId);
        return child != null && parent != null
                && child.getLft() > parent.getLft()
                && child.getRgt() < parent.getRgt();
    }

    @Override
    public int updateDisplayFlag(Menu menu) {
        return mapper.updateDisplayFlag(MenuPersistenceAssembler.toDataObject(menu));
    }

    @Override
    public void deleteMenuRole(Menu menu) {
        mapper.deleteMenuRole(MenuPersistenceAssembler.toDataObject(menu));
    }

    private Integer allocateInsertPosition(MenuDO node) {
        normalizeParentId(node);
        if (StringUtils.isNotBlank(node.getParentId())
                && !StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID)) {
            MenuDO parent = mapper.getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = mapper.getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(MenuDO oldNode, String parentId) {
        Integer newPosition = getInsertPosition(parentId);
        mapper.moveTreeRgts(newPosition, treeSpan(oldNode));
        mapper.moveTreeLfts(newPosition, treeSpan(oldNode));

        oldNode = mapper.getTreeNode(oldNode.getId());
        int offset = newPosition - oldNode.getLft();
        mapper.moveTreeNodes(oldNode.getLft(), oldNode.getRgt(), offset);

        mapper.moveTreeRgts(oldNode.getLft(), -treeSpan(oldNode));
        mapper.moveTreeLfts(oldNode.getLft(), -treeSpan(oldNode));
    }

    private Integer getInsertPosition(String parentId) {
        if (StringUtils.isNotBlank(parentId) && !StringUtils.equals(parentId, TreeEntity.ROOT_ID)) {
            MenuDO parent = mapper.getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = mapper.getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private static void normalizeParentId(MenuDO node) {
        if (node != null && (StringUtils.isBlank(node.getParentId())
                || StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(MenuDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
