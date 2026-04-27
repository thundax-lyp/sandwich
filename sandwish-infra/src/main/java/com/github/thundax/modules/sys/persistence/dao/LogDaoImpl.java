package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.persistence.assembler.LogPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import com.github.thundax.modules.sys.persistence.mapper.LogMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日志 DAO 实现。
 */
@Repository
public class LogDaoImpl implements LogDao {

    private final LogMapper mapper;

    public LogDaoImpl(LogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Log get(Log entity) {
        return LogPersistenceAssembler.toEntity(mapper.get(LogPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Log> getMany(List<String> idList) {
        return LogPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Log> findList(Log entity) {
        List<LogDO> dataObjects = mapper.findList(LogPersistenceAssembler.toDataObject(entity));
        List<Log> entities = LogPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Log entity) {
        return mapper.insert(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Log entity) {
        return mapper.update(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Log entity) {
        return mapper.updatePriority(LogPersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Log entity) {
        return mapper.updateStatus(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Log entity) {
        return mapper.updateDelFlag(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Log entity) {
        return mapper.delete(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int insertList(List<Log> list) {
        return mapper.insertList(LogPersistenceAssembler.toDataObjectList(list));
    }

    @Override
    public int batchDelete(Log log) {
        return mapper.batchDelete(LogPersistenceAssembler.toDataObject(log));
    }
}
