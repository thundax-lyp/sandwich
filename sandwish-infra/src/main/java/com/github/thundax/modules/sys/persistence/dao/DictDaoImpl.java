package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.thundax.common.utils.StringUtils;
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
        return DictPersistenceAssembler.toEntity(mapper.selectById(entity.getId()));
    }

    @Override
    public List<Dict> getMany(List<String> idList) {
        return DictPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Dict> findList(Dict entity) {
        List<DictDO> dataObjects = mapper.selectList(buildQueryWrapper(DictPersistenceAssembler.toDataObject(entity)));
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
        return mapper.deleteById(entity.getId());
    }

    @Override
    public List<String> findTypeList() {
        return mapper.findTypeList();
    }

    private QueryWrapper<DictDO> buildQueryWrapper(DictDO query) {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", DictDO.DEL_FLAG_NORMAL);
        if (query != null && StringUtils.isNotBlank(query.getQueryType())) {
            wrapper.eq("type", query.getQueryType());
        }
        if (query != null && StringUtils.isNotBlank(query.getQueryRemarks())) {
            wrapper.like("remarks", query.getQueryRemarks());
        }
        wrapper.orderByAsc("type", "priority", "create_date");
        return wrapper;
    }
}
