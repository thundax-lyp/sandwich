package com.github.thundax.common.web.context;

import com.github.thundax.common.context.SandwishContext;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public class DefaultSandwishContextResolver implements SandwishContextResolver {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_LOGIN_NAME = "X-Login-Name";
    public static final String HEADER_TOKEN = "Access-Token";

    @Override
    public SandwishContext resolve(HttpServletRequest request) {
        SandwishContext context = new SandwishContext();
        context.setRequestId(resolveRequestId(request));
        context.setUserId(StringUtils.trimToNull(request.getHeader(HEADER_USER_ID)));
        context.setLoginName(StringUtils.trimToNull(request.getHeader(HEADER_LOGIN_NAME)));
        context.setToken(StringUtils.trimToNull(request.getHeader(HEADER_TOKEN)));
        return context;
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = StringUtils.trimToNull(request.getHeader(HEADER_REQUEST_ID));
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }
}
