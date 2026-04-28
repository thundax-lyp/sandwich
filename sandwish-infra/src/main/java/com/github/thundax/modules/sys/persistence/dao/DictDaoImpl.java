package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.persistence.assembler.DictPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.cache.DictCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.DictDO;
import com.github.thundax.modules.sys.persistence.mapper.DictMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
    public Dict get(String id) {
        Dict dict = cacheSupport.getById(id);
        if (dict != null) {
            return dict;
        }

        dict = DictPersistenceAssembler.toEntity(mapper.selectById(id));
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
    public List<Dict> findList(String type, String label, String remarks) {
        return DictPersistenceAssembler.toEntityList(
                mapper.selectList(buildQueryWrapper(type, label, remarks)));
    }

    @Override
    public Page<Dict> findPage(
            String type, String label, String remarks, int pageNo, int pageSize) {
        Page<DictDO> dataObjectPage =
                mapper.selectPage(new Page<>(pageNo, pageSize), buildQueryWrapper(type, label, remarks));
        Page<Dict> entityPage =
                new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(DictPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int insert(Dict entity) {
        int count = mapper.insert(DictPersistenceAssembler.toDataObject(entity));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int update(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        int count =
                mapper.update(
                        null,
                        buildIdUpdateWrapper(dataObject)
                                .set(DictDO::getValue, dataObject.getValue())
                                .set(DictDO::getLabel, dataObject.getLabel())
                                .set(DictDO::getType, dataObject.getType())
                                .set(DictDO::getPriority, dataObject.getPriority())
                                .set(DictDO::getRemarks, dataObject.getRemarks())
                                .set(DictDO::getUpdateDate, dataObject.getUpdateDate())
                                .set(DictDO::getUpdateUserId, dataObject.getUpdateUserId())
                                .set(DictDO::getDelFlag, dataObject.getDelFlag()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int updatePriority(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        int count =
                mapper.update(
                        null,
                        buildIdUpdateWrapper(dataObject)
                                .set(DictDO::getPriority, dataObject.getPriority())
                                .set(DictDO::getUpdateDate, dataObject.getUpdateDate())
                                .set(DictDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeAll();
        return count;
    }

    public int updateStatus(Dict entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Dict entity) {
        DictDO dataObject = DictPersistenceAssembler.toDataObject(entity);
        int count =
                mapper.update(
                        null,
                        buildIdUpdateWrapper(dataObject)
                                .set(DictDO::getDelFlag, dataObject.getDelFlag())
                                .set(DictDO::getUpdateDate, dataObject.getUpdateDate())
                                .set(DictDO::getUpdateUserId, dataObject.getUpdateUserId()));
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public int delete(String id) {
        int count = mapper.deleteById(id);
        cacheSupport.removeAll();
        return count;
    }

    @Override
    public List<String> findTypeList() {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        wrapper.select("type").groupBy("type").orderByAsc("type");
        return mapper.selectObjs(wrapper).stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Override
    public String getDictionaryRevision() {
        return cacheSupport.currentVersion();
    }

    private QueryWrapper<DictDO> buildQueryWrapper(String type, String label, String remarks) {
        QueryWrapper<DictDO> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", DictDO.DEL_FLAG_NORMAL);
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(label)) {
            wrapper.like("label", label);
        }
        if (StringUtils.isNotBlank(remarks)) {
            wrapper.like("remarks", remarks);
        }
        wrapper.orderByAsc("type", "priority", "create_date");
        return wrapper;
    }

    private LambdaUpdateWrapper<DictDO> buildIdUpdateWrapper(DictDO dataObject) {
        LambdaUpdateWrapper<DictDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DictDO::getId, dataObject.getId());
        return wrapper;
    }
}
