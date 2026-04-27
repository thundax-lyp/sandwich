package com.github.thundax.modules.member.utils;

import com.github.thundax.common.collect.MapUtils;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wdit
 */
@Service
@Lazy(false)
public class MemberServiceHolder {

    private static MemberService service;

    private static final PooledThreadLocal<Map<String, Member>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

    @Autowired
    public MemberServiceHolder(MemberService targetService) {
        service = targetService;
    }

    public static MemberService getService() {
        if (service == null) {
            service = SpringContextHolder.getBean(MemberService.class);
        }
        return service;
    }

    public static Member get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return ID_OBJECT_HOLDER
                .computeIfAbsent(MapUtils::newHashMap)
                .computeIfAbsent(id, (key) -> getService().get(id));
    }

}
