package com.github.thundax.modules.member.utils;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.thread.PooledThreadLocal;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.member.entity.Member;
import com.github.thundax.modules.member.service.MemberService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class MemberServiceHolder {

    private static MemberService service;

    private static final PooledThreadLocal<Map<EntityId, Member>> ID_OBJECT_HOLDER = new PooledThreadLocal<>();

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

    public static Member get(EntityId id) {
        if (id == null) {
            return null;
        }
        return ID_OBJECT_HOLDER.computeIfAbsent(HashMap::new).computeIfAbsent(id, (key) -> getService()
                .get(id));
    }
}
