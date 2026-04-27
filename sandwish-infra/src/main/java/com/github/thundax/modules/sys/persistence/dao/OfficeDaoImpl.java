package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.persistence.assembler.OfficePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.OfficeDO;
import com.github.thundax.modules.sys.persistence.mapper.OfficeMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 机构 DAO 实现。
 */
@Repository
public class OfficeDaoImpl implements OfficeDao {

    private final OfficeMapper mapper;

    public OfficeDaoImpl(OfficeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Office get(Office entity) {
        return OfficePersistenceAssembler.toEntity(mapper.get(OfficePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Office> getMany(List<String> idList) {
        return OfficePersistenceAssembler.toEntityList(mapper.getMany(idList));
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
        return mapper.insert(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Office entity) {
        return mapper.update(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Office entity) {
        return mapper.updatePriority(OfficePersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Office entity) {
        return mapper.updateStatus(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Office entity) {
        return mapper.updateDelFlag(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Office entity) {
        return mapper.delete(OfficePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public Office getTreeNode(String id) {
        return OfficePersistenceAssembler.toEntity(mapper.getTreeNode(id));
    }

    @Override
    public void updateLftRgt(Office node) {
        mapper.updateLftRgt(OfficePersistenceAssembler.toDataObject(node));
    }

    @Override
    public void updateParent(Office node) {
        mapper.updateParent(OfficePersistenceAssembler.toDataObject(node));
    }

    @Override
    public Integer getMaxPosition() {
        return mapper.getMaxPosition();
    }

    @Override
    public void moveTreeRgts(Integer from, Integer offset) {
        mapper.moveTreeRgts(from, offset);
    }

    @Override
    public void moveTreeLfts(Integer from, Integer offset) {
        mapper.moveTreeLfts(from, offset);
    }

    @Override
    public void moveTreeNodes(Integer from, Integer to, Integer offset) {
        mapper.moveTreeNodes(from, to, offset);
    }
}
