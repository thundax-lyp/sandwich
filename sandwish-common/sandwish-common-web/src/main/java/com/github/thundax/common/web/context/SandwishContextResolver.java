package com.github.thundax.common.web.context;

import com.github.thundax.common.context.SandwishContext;
import javax.servlet.http.HttpServletRequest;

public interface SandwishContextResolver {

    SandwishContext resolve(HttpServletRequest request);
}
