package com.github.thundax.common.web.context;

import javax.servlet.http.HttpServletRequest;

public interface SandwishContextResolver {

    String resolveRequestId(HttpServletRequest request);
}
