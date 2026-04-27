package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.persistence.assembler.OfficePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.OfficeCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import com.github.thundax.modules.sys.persistence.mapper.OfficeMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 机构 DAO 实现。
 */
@Repository
public class OfficeDaoImpl implements OfficeDao {

    private final OfficeMapper mapper;
    private final OfficeCacheSupport cacheSupport;

    public OfficeDaoImpl(OfficeMapper mapper, OfficeCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Office get(Office entity) {
        Office office = cacheSupport.getById(entity.getId());
        if (office != null) {
            return office;
        }

        office = OfficePersistenceAssembler.toEntity(mapper.get(OfficePersistenceAssembler.toDataObject(entity)));
        cacheSupport.putById(office);
        return office;
    }

    @Override
    public List<Office> getMany(List<String> idList) {
        List<Office> officeList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Office office = cacheSupport.getById(id);
            if (office == null) {
                uncachedIdList.add(id);
            } else {
                officeList.add(office);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<Office> uncachedOfficeList = OfficePersistenceAssembler.toEntityList(mapper.getMany(uncachedIdList));
            for (Office office : uncachedOfficeList) {
                cacheSupport.putById(office);
                officeList.add(office);
            }
        }
        return officeList;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Office> findList(Office entity) {
        List<OfficeDO> dataObjects = mapper.findList(OfficePersistenceAssembler.toDataObject(entity));
        List<Office> entities = OfficePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Office entity) {
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(dataObject.getParentId());
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        mapper.moveTreeRgts(newPosition, 2);
        mapper.moveTreeLfts(newPosition, 2);
        int count = mapper.insert(dataObject);
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int update(Office entity) {
        OfficeDO oldNode = mapper.getTreeNode(entity.getId());
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(dataObject.getParentId());
        if (oldNode != null && !StringUtils.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(dataObject);
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Office entity) {
        int count = mapper.updatePriority(OfficePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    public int updateStatus(Office entity) {
        return mapper.updateStatus(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Office entity) {
        int count = mapper.updateDelFlag(OfficePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(Office entity) {
        OfficeDO node = mapper.getTreeNode(entity.getId());
        if (node == null) {
            return 0;
        }
        mapper.moveTreeRgts(node.getLft(), -treeSpan(node));
        mapper.moveTreeLfts(node.getLft(), -treeSpan(node));
        int count = mapper.delete(OfficePersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public void moveTreeNode(String fromId, String toId, TreeService.MoveTreeNodeType moveType) {
        OfficeDO fromNode = mapper.getTreeNode(fromId);
        OfficeDO toNode = mapper.getTreeNode(toId);

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

        OfficeDO parentUpdate = new OfficeDO();
        parentUpdate.setId(fromId);
        parentUpdate.setParentId(newParentId);
        mapper.updateParent(parentUpdate);
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(String childId, String parentId) {
        OfficeDO child = mapper.getTreeNode(childId);
        OfficeDO parent = mapper.getTreeNode(parentId);
        return child != null && parent != null
                && child.getLft() > parent.getLft()
                && child.getRgt() < parent.getRgt();
    }

    private Integer allocateInsertPosition(OfficeDO node) {
        normalizeParentId(node);
        if (StringUtils.isNotBlank(node.getParentId())
                && !StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID)) {
            OfficeDO parent = mapper.getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = mapper.getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(OfficeDO oldNode, String parentId) {
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
            OfficeDO parent = mapper.getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = mapper.getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private static void normalizeParentId(OfficeDO node) {
        if (node != null && (StringUtils.isBlank(node.getParentId())
                || StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(OfficeDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
