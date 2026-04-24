package com.github.thundax.modules.member.security;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.UserFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author wdit
 */
public class MemberUserFilter extends UserFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (super.isAccessAllowed(request, response, mappedValue)) {
            Subject subject = getSubject(request, response);
            Object principal = subject.getPrincipal();
            return principal instanceof MemberPrincipal;
        } else {
            return false;
        }
    }

}
