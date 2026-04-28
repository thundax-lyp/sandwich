package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.persistence.assembler.OfficePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.OfficeCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import com.github.thundax.modules.sys.persistence.mapper.OfficeMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class OfficeDaoImpl implements OfficeDao {

    private final OfficeMapper mapper;
    private final OfficeCacheSupport cacheSupport;

    public OfficeDaoImpl(OfficeMapper mapper, OfficeCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Office get(String id) {
        Office office = cacheSupport.getById(id);
        if (office != null) {
            return office;
        }

        office = OfficePersistenceAssembler.toEntity(mapper.selectById(id));
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
            List<Office> uncachedOfficeList =
                    OfficePersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Office office : uncachedOfficeList) {
                cacheSupport.putById(office);
                officeList.add(office);
            }
        }
        return officeList;
    }

    @Override
    public List<Office> findList(String parentId, String name, String remarks) {
        return OfficePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper(parentId, name, remarks)));
    }

    @Override
    public Page<Office> findPage(String parentId, String name, String remarks, int pageNo, int pageSize) {
        IPage<OfficeDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(parentId, name, remarks));
        Page<Office> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(OfficePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int insert(Office entity) {
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(dataObject.getParentId());
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        moveTreeRgts(newPosition, 2);
        moveTreeLfts(newPosition, 2);
        int count = mapper.insert(dataObject);
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int update(Office entity) {
        OfficeDO oldNode = getTreeNode(entity.getId());
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(dataObject.getParentId());
        if (oldNode != null && !StringUtils.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OfficeDO::getParentId, dataObject.getParentId())
                        .set(OfficeDO::getName, dataObject.getName())
                        .set(OfficeDO::getShortName, dataObject.getShortName())
                        .set(OfficeDO::getPriority, dataObject.getPriority())
                        .set(OfficeDO::getRemarks, dataObject.getRemarks())
                        .set(OfficeDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(OfficeDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Office entity) {
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(OfficeDO::getPriority, dataObject.getPriority()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int updateDelFlag(Office entity) {
        OfficeDO dataObject = OfficePersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(OfficeDO::getDelFlag, dataObject.getDelFlag())
                        .set(OfficeDO::getUpdateDate, dataObject.getUpdateDate())
                        .set(OfficeDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeById(entity.getId());
        return count;
    }

    @Override
    public int delete(String id) {
        OfficeDO node = getTreeNode(id);
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
        OfficeDO fromNode = getTreeNode(fromId);
        OfficeDO toNode = getTreeNode(toId);

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

        OfficeDO parentUpdate = new OfficeDO();
        parentUpdate.setId(fromId);
        parentUpdate.setParentId(newParentId);
        updateParent(parentUpdate);
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(String childId, String parentId) {
        OfficeDO child = getTreeNode(childId);
        OfficeDO parent = getTreeNode(parentId);
        return child != null && parent != null && child.getLft() > parent.getLft() && child.getRgt() < parent.getRgt();
    }

    private Integer allocateInsertPosition(OfficeDO node) {
        normalizeParentId(node);
        if (StringUtils.isNotBlank(node.getParentId()) && !StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID)) {
            OfficeDO parent = getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(OfficeDO oldNode, String parentId) {
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
            OfficeDO parent = getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private OfficeDO getTreeNode(String id) {
        return mapper.selectById(id);
    }

    private Integer getMaxPosition() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<OfficeDO>().select("MAX(rgt)"));
        if (maxValues == null || maxValues.isEmpty() || maxValues.get(0) == null) {
            return null;
        }
        return ((Number) maxValues.get(0)).intValue();
    }

    private void updateParent(OfficeDO node) {
        mapper.update(null, buildIdUpdateWrapper(node).set(OfficeDO::getParentId, node.getParentId()));
    }

    private void moveTreeRgts(Integer from, Integer offset) {
        LambdaUpdateWrapper<OfficeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(OfficeDO::getRgt, from).setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeLfts(Integer from, Integer offset) {
        LambdaUpdateWrapper<OfficeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(OfficeDO::getLft, from).setSql("lft = lft + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeNodes(Integer from, Integer to, Integer offset) {
        LambdaUpdateWrapper<OfficeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(OfficeDO::getLft, from, to)
                .setSql("lft = lft + " + offset)
                .setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private LambdaUpdateWrapper<OfficeDO> buildIdUpdateWrapper(OfficeDO dataObject) {
        LambdaUpdateWrapper<OfficeDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OfficeDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<OfficeDO> buildListWrapper(String parentId, String name, String remarks) {
        LambdaQueryWrapper<OfficeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OfficeDO::getDelFlag, OfficeDO.DEL_FLAG_NORMAL);
        if (StringUtils.isNotBlank(parentId)) {
            if (StringUtils.equals(parentId, TreeEntity.ROOT_ID)) {
                wrapper.isNull(OfficeDO::getParentId);
            } else {
                wrapper.eq(OfficeDO::getParentId, parentId);
            }
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.and(nested -> nested.like(OfficeDO::getName, name).or().like(OfficeDO::getShortName, name));
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(OfficeDO::getRemarks, remarks);
        }
        wrapper.orderByAsc(OfficeDO::getLft);
        return wrapper;
    }

    private static void normalizeParentId(OfficeDO node) {
        if (node != null
                && (StringUtils.isBlank(node.getParentId())
                        || StringUtils.equals(node.getParentId(), TreeEntity.ROOT_ID))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(OfficeDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
