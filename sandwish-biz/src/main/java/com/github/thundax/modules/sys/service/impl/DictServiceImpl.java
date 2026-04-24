package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.service.DictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class DictServiceImpl extends CrudServiceImpl<DictDao, Dict> implements DictService {

    public DictServiceImpl(DictDao dao, RedisClient redisClient) {
        super(dao, redisClient);
    }

    @Override
    protected void initialize() {
        super.initialize();
        removeAllCache();
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "sys.dict.";
    }

    @Override
    public List<String> findTypeList() {
        return dao.findTypeList();
    }


    public List<String> findLabelList(String type){
      List<String> result=new ArrayList<String>();
        Dict query=new Dict();
        query.setQueryProp(Dict.Query.PROP_TYPE,type);
        List<Dict> list=dao.findList(query);
        String s = "";
        for (Dict item : list) {
            s = item.getLabel();
            if (StringUtils.isNotEmpty(s)) {
                result.add(s);
            }
        }
      return result;
    }

    @Override
    public List<Dict> findList(Dict dict) {
        return dao.findList(dict);
    }

    @Override
    public void removeCache(Dict dict) {
        super.removeAllCache();
    }

}
