package com.github.thundax.common.web.context;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public class DefaultSandwishContextResolver implements SandwishContextResolver {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    public String resolveRequestId(HttpServletRequest request) {
        String requestId = StringUtils.trimToNull(request.getHeader(HEADER_REQUEST_ID));
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }
}
