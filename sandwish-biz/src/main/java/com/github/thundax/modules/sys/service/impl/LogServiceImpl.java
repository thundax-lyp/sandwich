package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.LogDao;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.service.LogService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class LogServiceImpl extends CrudServiceImpl<LogDao, Log> implements LogService {

    private static final int BATCH_INSERT_SIZE = 50;
    private final SignService signService;

    public LogServiceImpl(LogDao dao, SignService signService) {
        super(dao);
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
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int count = 0;

        int pageSize = BATCH_INSERT_SIZE;
        int totalPage = (list.size() + pageSize - 1) / pageSize;
        for (int pageNo = 0; pageNo < totalPage; pageNo++) {
            int fromIndex = pageSize * pageNo;
            int toIndex = Math.min(fromIndex + pageSize, list.size());
            List<Log> subList = new ArrayList<>(list.subList(fromIndex, toIndex));
            subList.forEach(Log::preInsert);
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
