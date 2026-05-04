package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.tree.TreeNodeMoveType;
import com.github.thundax.modules.sys.dao.DepartmentDao;
import com.github.thundax.modules.sys.entity.Department;
import com.github.thundax.modules.sys.persistence.assembler.DepartmentPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.DepartmentCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.DepartmentDO;
import com.github.thundax.modules.sys.persistence.mapper.DepartmentMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentDaoImpl implements DepartmentDao {

    private static final String DEL_FLAG_COLUMN = "del_flag";
    private static final String NORMAL_DEL_FLAG = "0";
    private static final String ROOT_ID = "ROOT";

    private final DepartmentMapper mapper;
    private final DepartmentCacheSupport cacheSupport;

    public DepartmentDaoImpl(DepartmentMapper mapper, DepartmentCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Department getById(EntityId id) {
        Department department = cacheSupport.getById(id.value());
        if (department != null) {
            return department;
        }

        department = DepartmentPersistenceAssembler.toEntity(mapper.selectById(id.value()));
        cacheSupport.putById(department);
        return department;
    }

    @Override
    public List<Department> listByIds(List<String> idList) {
        List<Department> departmentList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Department department = cacheSupport.getById(id);
            if (department == null) {
                uncachedIdList.add(id);
            } else {
                departmentList.add(department);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<Department> uncachedDepartmentList =
                    DepartmentPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Department department : uncachedDepartmentList) {
                cacheSupport.putById(department);
                departmentList.add(department);
            }
        }
        return departmentList;
    }

    @Override
    public List<Department> list(String parentId, String name, String remarks) {
        return DepartmentPersistenceAssembler.toEntityList(
                mapper.selectList(buildListWrapper(parentId, name, remarks)));
    }

    @Override
    public Page<Department> page(String parentId, String name, String remarks, int pageNo, int pageSize) {
        IPage<DepartmentDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper(parentId, name, remarks));
        Page<Department> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(DepartmentPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Department entity) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(entity);
        Integer newPosition = allocateInsertPosition(dataObject);
        entity.setParentId(dataObject.getParentId());
        dataObject.setLft(newPosition);
        dataObject.setRgt(newPosition + 1);
        moveTreeRgts(newPosition, 2);
        moveTreeLfts(newPosition, 2);
        mapper.insert(dataObject);
        mapper.update(
                null,
                new UpdateWrapper<DepartmentDO>()
                        .set(DEL_FLAG_COLUMN, NORMAL_DEL_FLAG)
                        .eq("id", dataObject.getId()));
        cacheSupport.removeAll();
        return dataObject.getId();
    }

    @Override
    public int update(Department entity) {
        DepartmentDO oldNode = getTreeNode(EntityIdCodec.toValue(entity.getId()));
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(entity);
        normalizeParentId(dataObject);
        entity.setParentId(dataObject.getParentId());
        if (oldNode != null && !StringUtils.equals(oldNode.getParentId(), dataObject.getParentId())) {
            moveNodeToParent(oldNode, dataObject.getParentId());
        }
        int count = mapper.update(
                null,
                buildIdUpdateWrapper(dataObject)
                        .set(DepartmentDO::getParentId, dataObject.getParentId())
                        .set(DepartmentDO::getName, dataObject.getName())
                        .set(DepartmentDO::getShortName, dataObject.getShortName())
                        .set(DepartmentDO::getPriority, dataObject.getPriority())
                        .set(DepartmentDO::getRemarks, dataObject.getRemarks()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Department entity) {
        DepartmentDO dataObject = DepartmentPersistenceAssembler.toDataObject(entity);
        int count = mapper.update(
                null, buildIdUpdateWrapper(dataObject).set(DepartmentDO::getPriority, dataObject.getPriority()));
        cacheSupport.removeById(EntityIdCodec.toValue(entity.getId()));
        return count;
    }

    @Override
    public int deleteById(EntityId id) {
        DepartmentDO node = getTreeNode(id.value());
        if (node == null) {
            return 0;
        }
        moveTreeRgts(node.getLft(), -treeSpan(node));
        moveTreeLfts(node.getLft(), -treeSpan(node));
        int count = mapper.deleteById(id.value());
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public void moveTreeNode(String fromId, String toId, TreeNodeMoveType moveType) {
        DepartmentDO fromNode = getTreeNode(fromId);
        DepartmentDO toNode = getTreeNode(toId);

        int newPosition;
        String newParentId;
        if (moveType == TreeNodeMoveType.AFTER) {
            newPosition = toNode.getRgt() + 1;
            newParentId = toNode.getParentId();
        } else if (moveType == TreeNodeMoveType.BEFORE) {
            newPosition = toNode.getLft();
            newParentId = toNode.getParentId();
        } else if (moveType == TreeNodeMoveType.INSIDE) {
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

        updateParent(DepartmentPersistenceAssembler.toParentUpdateDataObject(fromId, newParentId));
        cacheSupport.removeAll();
    }

    @Override
    public boolean isChildOf(String childId, String parentId) {
        DepartmentDO child = getTreeNode(childId);
        DepartmentDO parent = getTreeNode(parentId);
        return child != null && parent != null && child.getLft() > parent.getLft() && child.getRgt() < parent.getRgt();
    }

    private Integer allocateInsertPosition(DepartmentDO node) {
        normalizeParentId(node);
        if (StringUtils.isNotBlank(node.getParentId()) && !StringUtils.equals(node.getParentId(), ROOT_ID)) {
            DepartmentDO parent = getTreeNode(node.getParentId());
            return parent.getRgt();
        }

        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private void moveNodeToParent(DepartmentDO oldNode, String parentId) {
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
        if (StringUtils.isNotBlank(parentId) && !StringUtils.equals(parentId, ROOT_ID)) {
            DepartmentDO parent = getTreeNode(parentId);
            return parent.getRgt();
        }
        Integer maxRgt = getMaxPosition();
        if (maxRgt == null) {
            maxRgt = 0;
        }
        return maxRgt + 1;
    }

    private DepartmentDO getTreeNode(String id) {
        return mapper.selectById(id);
    }

    private Integer getMaxPosition() {
        List<Object> maxValues = mapper.selectObjs(new QueryWrapper<DepartmentDO>().select("MAX(rgt)"));
        if (maxValues == null || maxValues.isEmpty() || maxValues.get(0) == null) {
            return null;
        }
        return ((Number) maxValues.get(0)).intValue();
    }

    private void updateParent(DepartmentDO node) {
        mapper.update(null, buildIdUpdateWrapper(node).set(DepartmentDO::getParentId, node.getParentId()));
    }

    private void moveTreeRgts(Integer from, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(DepartmentDO::getRgt, from).setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeLfts(Integer from, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.ge(DepartmentDO::getLft, from).setSql("lft = lft + " + offset);
        mapper.update(null, wrapper);
    }

    private void moveTreeNodes(Integer from, Integer to, Integer offset) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.between(DepartmentDO::getLft, from, to)
                .setSql("lft = lft + " + offset)
                .setSql("rgt = rgt + " + offset);
        mapper.update(null, wrapper);
    }

    private LambdaUpdateWrapper<DepartmentDO> buildIdUpdateWrapper(DepartmentDO dataObject) {
        LambdaUpdateWrapper<DepartmentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DepartmentDO::getId, dataObject.getId());
        return wrapper;
    }

    private LambdaQueryWrapper<DepartmentDO> buildListWrapper(String parentId, String name, String remarks) {
        LambdaQueryWrapper<DepartmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("del_flag = {0}", NORMAL_DEL_FLAG);
        if (StringUtils.isNotBlank(parentId)) {
            if (StringUtils.equals(parentId, ROOT_ID)) {
                wrapper.isNull(DepartmentDO::getParentId);
            } else {
                wrapper.eq(DepartmentDO::getParentId, parentId);
            }
        }
        if (StringUtils.isNotBlank(name)) {
            wrapper.and(nested -> nested.like(DepartmentDO::getName, name).or().like(DepartmentDO::getShortName, name));
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like(DepartmentDO::getRemarks, remarks);
        }
        wrapper.orderByAsc(DepartmentDO::getLft);
        return wrapper;
    }

    private static void normalizeParentId(DepartmentDO node) {
        if (node != null
                && (StringUtils.isBlank(node.getParentId()) || StringUtils.equals(node.getParentId(), ROOT_ID))) {
            node.setParentId(null);
        }
    }

    private static int treeSpan(DepartmentDO node) {
        return node.getRgt() - node.getLft() + 1;
    }
}
