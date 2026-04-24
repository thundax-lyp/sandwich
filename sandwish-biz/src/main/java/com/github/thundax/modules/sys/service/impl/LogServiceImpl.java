package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class LogServiceImpl extends CrudServiceImpl<LogDao, Log> implements LogService {

    private static final int BATCH_INSERT_SIZE = 50;
    private final SignService signService;

    public LogServiceImpl(LogDao dao, RedisClient redisClient, SignService signService) {
        super(dao, redisClient);
        this.signService = signService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Log log) {
        super.save(log);

        if (log.isSignable()) {
            signService.sign(log.getSignName(), log.getSignId(), log.getSignBody());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertList(List<Log> list) {
        if (ListUtils.isEmpty(list)) {
            return 0;
        }

        int count = 0;

        int pageSize = BATCH_INSERT_SIZE;
        int totalPage = (list.size() + pageSize - 1) / pageSize;
        for (int pageNo = 0; pageNo < totalPage; pageNo++) {
            List<Log> subList = ListUtils.subList(list, pageSize * pageNo, pageSize);
            ListUtils.forEach(subList, Log::preInsert);
            count += dao.insertList(subList);
        }

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(Log log) {
        return dao.batchDelete(log);
    }

}
