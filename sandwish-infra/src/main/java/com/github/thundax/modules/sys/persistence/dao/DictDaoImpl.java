package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.persistence.assembler.DictPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import com.github.thundax.modules.sys.persistence.mapper.DictMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 字典 DAO 实现。
 */
@Repository
public class DictDaoImpl implements DictDao {

    private final DictMapper mapper;

    public DictDaoImpl(DictMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Dict get(Dict entity) {
        return DictPersistenceAssembler.toEntity(mapper.get(DictPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Dict> getMany(List<String> idList) {
        return DictPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Dict> findList(Dict entity) {
        List<DictDO> dataObjects = mapper.findList(DictPersistenceAssembler.toDataObject(entity));
        List<Dict> entities = DictPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Dict entity) {
        return mapper.insert(DictPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Dict entity) {
        return mapper.update(DictPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Dict entity) {
        return mapper.updatePriority(DictPersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Dict entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Dict entity) {
        return mapper.updateDelFlag(DictPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Dict entity) {
        return mapper.delete(DictPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public List<String> findTypeList() {
        return mapper.findTypeList();
    }
}
