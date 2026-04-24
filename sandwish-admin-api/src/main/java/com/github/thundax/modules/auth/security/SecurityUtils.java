package com.github.thundax.modules.auth.security;

import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.auth.security.service.SubjectService;
import com.github.thundax.modules.auth.security.subject.Subject;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author thundax
 */
@Service
public class SecurityUtils {

    private static SubjectService subjectService;

    @Autowired
    public SecurityUtils(SubjectService service) {
        subjectService = service;
    }

    private static SubjectService getService() {
        if (subjectService == null) {
            subjectService = SpringContextHolder.getBean(SubjectService.class);
        }
        return subjectService;
    }

    public static Subject getSubject() {
        String userId = UserAccessHolder.currentUserId();
        if (userId != null) {
            return getService().getSubject(userId);
        }
        return null;
    }

}
