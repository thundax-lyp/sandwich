package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.service.impl.TreeServiceImpl;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class OfficeServiceImpl extends TreeServiceImpl<OfficeDao, Office> implements OfficeService {

    public OfficeServiceImpl(OfficeDao dao, RedisClient redisClient) {
        super(dao, redisClient);
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "SYS_OFFICE_";
    }

}
