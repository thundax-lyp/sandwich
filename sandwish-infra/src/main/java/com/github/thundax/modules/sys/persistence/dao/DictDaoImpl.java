package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.persistence.assembler.DictPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.DictCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import com.github.thundax.modules.sys.persistence.mapper.DictMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

/** 字典 DAO 实现。 */
@Repository
public class DictDaoImpl implements DictDao {

    private final DictMapper mapper;
    private final DictCacheSupport cacheSupport;

    public DictDaoImpl(DictMapper mapper, DictCacheSupport cacheSupport) {
        this.mapper = mapper;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public Dict get(Dict entity) {
        Dict dict = cacheSupport.getById(entity.getId());
        if (dict != null) {
            return dict;
        }

        dict = DictPersistenceAssembler.toEntity(mapper.selectById(entity.getId()));
        cacheSupport.putById(dict);
        return dict;
    }

    @Override
    public List<Dict> getMany(List<String> idList) {
        List<Dict> dictList = new ArrayList<>();
        List<String> uncachedIdList = new ArrayList<>();
        for (String id : idList) {
            Dict dict = cacheSupport.getById(id);
            if (dict == null) {
                uncachedIdList.add(id);
            } else {
                dictList.add(dict);
            }
        }

        if (!uncachedIdList.isEmpty()) {
            List<Dict> uncachedDictList =
                    DictPersistenceAssembler.toEntityList(mapper.selectBatchIds(uncachedIdList));
            for (Dict dict : uncachedDictList) {
                cacheSupport.putById(dict);
                dictList.add(dict);
            }
        }
        return dictList;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Dict> findList(Dict entity) {
        List<DictDO> dataObjects =
                mapper.selectList(buildQueryWrapper(DictPersistenceAssembler.toDataObject(entity)));
        List<Dict> entities = DictPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Dict entity) {
        int count = mapper.insert(DictPersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int update(Dict entity) {
        int count = mapper.update(DictPersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Dict entity) {
        int count = mapper.updatePriority(DictPersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    public int updateStatus(Dict entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Dict entity) {
        int count = mapper.updateDelFlag(DictPersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int delete(Dict entity) {
        int count = mapper.deleteById(entity.getId());
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public List<String> findTypeList() {
        return mapper.findTypeList();
    }

    @Override
    public String getDictionaryRevision() {
        return cacheSupport.currentVersion();
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
