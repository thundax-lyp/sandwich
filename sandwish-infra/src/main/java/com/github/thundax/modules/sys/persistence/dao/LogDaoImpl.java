package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.persistence.assembler.LogPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.LogDO;
import com.github.thundax.modules.sys.persistence.mapper.LogMapper;
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
    public Log get(String id) {
        return LogPersistenceAssembler.toEntity(mapper.selectById(id));
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
    public int insert(Log entity) {
        return mapper.insert(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Log entity) {
        return mapper.updateById(LogPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(String id) {
        return mapper.deleteById(id);
    }

    @Override
    public int insertList(List<Log> list) {
        int count = 0;
        for (LogDO dataObject : LogPersistenceAssembler.toDataObjectList(list)) {
            count += mapper.insert(dataObject);
        }
        return count;
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
