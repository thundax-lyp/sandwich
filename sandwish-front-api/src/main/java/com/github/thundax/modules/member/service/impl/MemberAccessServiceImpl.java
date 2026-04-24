package com.github.thundax.modules.member.service.impl;

import com.github.thundax.modules.member.security.MemberPrincipal;
import com.github.thundax.modules.member.service.MemberAccessService;
import com.github.thundax.modules.member.utils.ShiroUtils;
import org.springframework.stereotype.Service;


/**
 * @author wdit
 */
@Service
public class MemberAccessServiceImpl implements MemberAccessService {

    @Override
    public String getCurrentMemberId() {
        MemberPrincipal principal = ShiroUtils.getPrincipal();
        if (principal != null) {
            return principal.getId();
        }

        return null;
    }

    @Override
    public Object getSessionCache(String name) {
        return ShiroUtils.getSessionCache(name);
    }

    @Override
    public void setSessionCache(String name, Object value) {
        ShiroUtils.putSessionCache(name, value);
    }

}
