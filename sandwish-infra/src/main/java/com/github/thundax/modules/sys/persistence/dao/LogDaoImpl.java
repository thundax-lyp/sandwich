package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.persistence.assembler.LogPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import com.github.thundax.modules.sys.persistence.mapper.LogMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class LogDaoImpl implements LogDao {

    private final LogMapper mapper;

    public LogDaoImpl(LogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Log get(EntityId id) {
        return LogPersistenceAssembler.toEntity(mapper.selectById(id.value()));
    }

    @Override
    public List<Log> getMany(List<String> idList) {
        return LogPersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<Log> findList(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate) {
        return LogPersistenceAssembler.toEntityList(mapper.selectList(
                buildListWrapper(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate)));
    }

    @Override
    public Page<Log> findPage(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate,
            int pageNo,
            int pageSize) {
        Page<LogDO> page = new Page<>(pageNo, pageSize);
        IPage<LogDO> dataObjectPage = mapper.selectPage(
                page,
                buildListWrapper(type, remoteAddr, userLoginName, userName, title, requestUri, beginDate, endDate));
        Page<Log> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(LogPersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public String insert(Log entity) {
        LogDO dataObject = LogPersistenceAssembler.toDataObject(entity);
        mapper.insert(dataObject);
        return dataObject.getId();
    }

    @Override
    public int update(Log entity) {
        return mapper.updateById(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(EntityId id) {
        return mapper.deleteById(id.value());
    }

    @Override
    public List<String> insertList(List<Log> list) {
        List<String> idList = new ArrayList<>();
        for (LogDO dataObject : LogPersistenceAssembler.toDataObjectList(list)) {
            mapper.insert(dataObject);
            idList.add(dataObject.getId());
        }
        return idList;
    }

    @Override
    public int batchDelete(
            String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate) {
        return mapper.delete(buildBatchDeleteWrapper(type, remoteAddr, title, requestUri, beginDate, endDate));
    }

    private QueryWrapper<LogDO> buildListWrapper(
            String type,
            String remoteAddr,
            String userLoginName,
            String userName,
            String title,
            String requestUri,
            Date beginDate,
            Date endDate) {
        QueryWrapper<LogDO> wrapper = buildBatchDeleteWrapper(type, remoteAddr, title, requestUri, beginDate, endDate);
        if (StringUtils.isNotBlank(userLoginName)) {
            wrapper.apply(
                    "user_id IN (SELECT id FROM sys_user WHERE login_name LIKE CONCAT('%', {0}, '%'))", userLoginName);
        }
        if (StringUtils.isNotBlank(userName)) {
            wrapper.apply("user_id IN (SELECT id FROM sys_user WHERE name LIKE CONCAT('%', {0}, '%'))", userName);
        }
        wrapper.orderByDesc("log_date");
        return wrapper;
    }

    private QueryWrapper<LogDO> buildBatchDeleteWrapper(
            String type, String remoteAddr, String title, String requestUri, Date beginDate, Date endDate) {
        QueryWrapper<LogDO> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(remoteAddr)) {
            wrapper.eq("remote_addr", remoteAddr);
        }
        if (StringUtils.isNotBlank(title)) {
            wrapper.like("title", title);
        }
        if (StringUtils.isNotBlank(requestUri)) {
            wrapper.like("request_uri", requestUri);
        }
        if (beginDate != null && endDate != null) {
            wrapper.between("log_date", beginDate, endDate);
        }
        return wrapper;
    }
}
